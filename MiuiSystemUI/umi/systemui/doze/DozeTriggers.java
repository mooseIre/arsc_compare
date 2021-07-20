package com.android.systemui.doze;

import android.app.AlarmManager;
import android.app.UiModeManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.AmbientDisplayConfiguration;
import android.metrics.LogMaker;
import android.os.SystemClock;
import android.text.format.Formatter;
import android.util.Log;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.logging.UiEventLoggerImpl;
import com.android.systemui.Dependency;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.dock.DockManager;
import com.android.systemui.doze.DozeHost;
import com.android.systemui.doze.DozeMachine;
import com.android.systemui.doze.DozeSensors;
import com.android.systemui.doze.DozeTriggers;
import com.android.systemui.statusbar.phone.DozeParameters;
import com.android.systemui.util.Assert;
import com.android.systemui.util.sensors.AsyncSensorManager;
import com.android.systemui.util.sensors.ProximitySensor;
import com.android.systemui.util.wakelock.WakeLock;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class DozeTriggers implements DozeMachine.Part {
    private static final boolean DEBUG = DozeService.DEBUG;
    private static final UiEventLogger UI_EVENT_LOGGER = new UiEventLoggerImpl();
    private static boolean sWakeDisplaySensorState = true;
    private final boolean mAllowPulseTriggers;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private final TriggerReceiver mBroadcastReceiver = new TriggerReceiver();
    private final AmbientDisplayConfiguration mConfig;
    private final Context mContext;
    private final DockEventListener mDockEventListener = new DockEventListener();
    private final DockManager mDockManager;
    private final DozeHost mDozeHost;
    private final DozeLog mDozeLog;
    private final DozeParameters mDozeParameters;
    private final DozeSensors mDozeSensors;
    private DozeHost.Callback mHostCallback = new DozeHost.Callback() {
        /* class com.android.systemui.doze.DozeTriggers.AnonymousClass1 */

        @Override // com.android.systemui.doze.DozeHost.Callback
        public void onNotificationAlerted(Runnable runnable) {
            DozeTriggers.this.onNotification(runnable);
        }

        @Override // com.android.systemui.doze.DozeHost.Callback
        public void onPowerSaveChanged(boolean z) {
            if (DozeTriggers.this.mDozeHost.isPowerSaveActive()) {
                DozeTriggers.this.mMachine.requestState(DozeMachine.State.DOZE);
            } else if (DozeTriggers.this.mMachine.getState() == DozeMachine.State.DOZE && DozeTriggers.this.mConfig.alwaysOnEnabled(-2)) {
                DozeTriggers.this.mMachine.requestState(DozeMachine.State.DOZE_AOD);
            }
        }

        @Override // com.android.systemui.doze.DozeHost.Callback
        public void onDozeSuppressedChanged(boolean z) {
            DozeMachine.State state;
            if (!DozeTriggers.this.mConfig.alwaysOnEnabled(-2) || z) {
                state = DozeMachine.State.DOZE;
            } else {
                state = DozeMachine.State.DOZE_AOD;
            }
            DozeTriggers.this.mMachine.requestState(state);
        }
    };
    private final DozeMachine mMachine;
    private final MetricsLogger mMetricsLogger = ((MetricsLogger) Dependency.get(MetricsLogger.class));
    private long mNotificationPulseTime;
    private final ProximitySensor.ProximityCheck mProxCheck;
    private boolean mPulsePending;
    private final AsyncSensorManager mSensorManager;
    private final UiModeManager mUiModeManager;
    private final WakeLock mWakeLock;

    public enum DozingUpdateUiEvent implements UiEventLogger.UiEventEnum {
        DOZING_UPDATE_NOTIFICATION(433),
        DOZING_UPDATE_SIGMOTION(434),
        DOZING_UPDATE_SENSOR_PICKUP(435),
        DOZING_UPDATE_SENSOR_DOUBLE_TAP(436),
        DOZING_UPDATE_SENSOR_LONG_SQUEEZE(437),
        DOZING_UPDATE_DOCKING(438),
        DOZING_UPDATE_SENSOR_WAKEUP(439),
        DOZING_UPDATE_SENSOR_WAKE_LOCKSCREEN(440),
        DOZING_UPDATE_SENSOR_TAP(441);
        
        private final int mId;

        private DozingUpdateUiEvent(int i) {
            this.mId = i;
        }

        public int getId() {
            return this.mId;
        }

        static DozingUpdateUiEvent fromReason(int i) {
            switch (i) {
                case 1:
                    return DOZING_UPDATE_NOTIFICATION;
                case 2:
                    return DOZING_UPDATE_SIGMOTION;
                case 3:
                    return DOZING_UPDATE_SENSOR_PICKUP;
                case 4:
                    return DOZING_UPDATE_SENSOR_DOUBLE_TAP;
                case 5:
                    return DOZING_UPDATE_SENSOR_LONG_SQUEEZE;
                case 6:
                    return DOZING_UPDATE_DOCKING;
                case 7:
                    return DOZING_UPDATE_SENSOR_WAKEUP;
                case 8:
                    return DOZING_UPDATE_SENSOR_WAKE_LOCKSCREEN;
                case 9:
                    return DOZING_UPDATE_SENSOR_TAP;
                default:
                    return null;
            }
        }
    }

    public DozeTriggers(Context context, DozeMachine dozeMachine, DozeHost dozeHost, AlarmManager alarmManager, AmbientDisplayConfiguration ambientDisplayConfiguration, DozeParameters dozeParameters, AsyncSensorManager asyncSensorManager, WakeLock wakeLock, boolean z, DockManager dockManager, ProximitySensor proximitySensor, ProximitySensor.ProximityCheck proximityCheck, DozeLog dozeLog, BroadcastDispatcher broadcastDispatcher) {
        this.mContext = context;
        this.mMachine = dozeMachine;
        this.mDozeHost = dozeHost;
        this.mConfig = ambientDisplayConfiguration;
        this.mDozeParameters = dozeParameters;
        this.mSensorManager = asyncSensorManager;
        this.mWakeLock = wakeLock;
        this.mAllowPulseTriggers = z;
        this.mDozeSensors = new DozeSensors(context, alarmManager, this.mSensorManager, dozeParameters, ambientDisplayConfiguration, wakeLock, new DozeSensors.Callback() {
            /* class com.android.systemui.doze.$$Lambda$XuSeOmLZ56lHJGoIP26_sIwbcBM */

            @Override // com.android.systemui.doze.DozeSensors.Callback
            public final void onSensorPulse(int i, float f, float f2, float[] fArr) {
                DozeTriggers.this.onSensor(i, f, f2, fArr);
            }
        }, new Consumer() {
            /* class com.android.systemui.doze.$$Lambda$DozeTriggers$ulqUMEXi8OgK7771oZ9BOr21BBk */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                DozeTriggers.lambda$ulqUMEXi8OgK7771oZ9BOr21BBk(DozeTriggers.this, ((Boolean) obj).booleanValue());
            }
        }, dozeLog, proximitySensor);
        this.mUiModeManager = (UiModeManager) this.mContext.getSystemService(UiModeManager.class);
        this.mDockManager = dockManager;
        this.mProxCheck = proximityCheck;
        this.mDozeLog = dozeLog;
        this.mBroadcastDispatcher = broadcastDispatcher;
    }

    @Override // com.android.systemui.doze.DozeMachine.Part
    public void destroy() {
        this.mDozeSensors.destroy();
    }

    private void onNotification(Runnable runnable) {
        if (DozeMachine.DEBUG) {
            Log.d("DozeTriggers", "requestNotificationPulse");
        }
        if (!sWakeDisplaySensorState) {
            Log.d("DozeTriggers", "Wake display false. Pulse denied.");
            runIfNotNull(runnable);
            this.mDozeLog.tracePulseDropped("wakeDisplaySensor");
            return;
        }
        this.mNotificationPulseTime = SystemClock.elapsedRealtime();
        if (!this.mConfig.pulseOnNotificationEnabled(-2)) {
            runIfNotNull(runnable);
            this.mDozeLog.tracePulseDropped("pulseOnNotificationsDisabled");
        } else if (this.mDozeHost.isDozeSuppressed()) {
            runIfNotNull(runnable);
            this.mDozeLog.tracePulseDropped("dozeSuppressed");
        } else {
            requestPulse(1, false, runnable);
            this.mDozeLog.traceNotificationPulse();
        }
    }

    private static void runIfNotNull(Runnable runnable) {
        if (runnable != null) {
            runnable.run();
        }
    }

    private void proximityCheckThenCall(Consumer<Boolean> consumer, boolean z, int i) {
        Boolean isProximityCurrentlyNear = this.mDozeSensors.isProximityCurrentlyNear();
        if (z) {
            consumer.accept(null);
        } else if (isProximityCurrentlyNear != null) {
            consumer.accept(isProximityCurrentlyNear);
        } else {
            this.mProxCheck.check(500, new Consumer(SystemClock.uptimeMillis(), i, consumer) {
                /* class com.android.systemui.doze.$$Lambda$DozeTriggers$7dHaL16QO2EYQ_3R1TKZzEi3lA */
                public final /* synthetic */ long f$1;
                public final /* synthetic */ int f$2;
                public final /* synthetic */ Consumer f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r4;
                    this.f$3 = r5;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    DozeTriggers.this.lambda$proximityCheckThenCall$0$DozeTriggers(this.f$1, this.f$2, this.f$3, (Boolean) obj);
                }
            });
            this.mWakeLock.acquire("DozeTriggers");
        }
    }

    /* access modifiers changed from: public */
    /* access modifiers changed from: private */
    /* renamed from: lambda$proximityCheckThenCall$0 */
    public /* synthetic */ void lambda$proximityCheckThenCall$0$DozeTriggers(long j, int i, Consumer consumer, Boolean bool) {
        boolean z;
        long uptimeMillis = SystemClock.uptimeMillis();
        DozeLog dozeLog = this.mDozeLog;
        if (bool == null) {
            z = false;
        } else {
            z = bool.booleanValue();
        }
        dozeLog.traceProximityResult(z, uptimeMillis - j, i);
        consumer.accept(bool);
        this.mWakeLock.release("DozeTriggers");
    }

    public void onSensor(int i, float f, float f2, float[] fArr) {
        boolean z = false;
        boolean z2 = i == 4;
        boolean z3 = i == 9;
        boolean z4 = i == 3;
        boolean z5 = i == 5;
        boolean z6 = i == 7;
        boolean z7 = i == 8;
        boolean z8 = (fArr == null || fArr.length <= 0 || fArr[0] == 0.0f) ? false : true;
        DozeMachine.State state = null;
        if (z6) {
            if (!this.mMachine.isExecutingTransition()) {
                state = this.mMachine.getState();
            }
            onWakeScreen(z8, state);
        } else if (z5) {
            requestPulse(i, true, null);
        } else if (!z7) {
            proximityCheckThenCall(new Consumer(z2, z3, f, f2, i, z4) {
                /* class com.android.systemui.doze.$$Lambda$DozeTriggers$_9uGVeOllRSk5IFkZMhDAbIz6Gw */
                public final /* synthetic */ boolean f$1;
                public final /* synthetic */ boolean f$2;
                public final /* synthetic */ float f$3;
                public final /* synthetic */ float f$4;
                public final /* synthetic */ int f$5;
                public final /* synthetic */ boolean f$6;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                    this.f$5 = r6;
                    this.f$6 = r7;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    DozeTriggers.this.lambda$onSensor$1$DozeTriggers(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, (Boolean) obj);
                }
            }, true, i);
        } else if (z8) {
            requestPulse(i, true, null);
        }
        if (z4) {
            if (SystemClock.elapsedRealtime() - this.mNotificationPulseTime < ((long) this.mDozeParameters.getPickupVibrationThreshold())) {
                z = true;
            }
            this.mDozeLog.tracePickupWakeUp(z);
        }
    }

    /* access modifiers changed from: public */
    /* access modifiers changed from: private */
    /* renamed from: lambda$onSensor$1 */
    public /* synthetic */ void lambda$onSensor$1$DozeTriggers(boolean z, boolean z2, float f, float f2, int i, boolean z3, Boolean bool) {
        if (bool != null && bool.booleanValue()) {
            return;
        }
        if (z || z2) {
            if (!(f == -1.0f || f2 == -1.0f)) {
                this.mDozeHost.onSlpiTap(f, f2);
            }
            gentleWakeUp(i);
        } else if (z3) {
            gentleWakeUp(i);
        } else {
            this.mDozeHost.extendPulse(i);
        }
    }

    private void gentleWakeUp(int i) {
        this.mMetricsLogger.write(new LogMaker(223).setType(6).setSubtype(i));
        Optional ofNullable = Optional.ofNullable(DozingUpdateUiEvent.fromReason(i));
        UiEventLogger uiEventLogger = UI_EVENT_LOGGER;
        Objects.requireNonNull(uiEventLogger);
        ofNullable.ifPresent(new Consumer(uiEventLogger) {
            /* class com.android.systemui.doze.$$Lambda$vBVHjIDgps_phZpQ4QNJ6P1upak */
            public final /* synthetic */ UiEventLogger f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.log((DozeTriggers.DozingUpdateUiEvent) obj);
            }
        });
        if (this.mDozeParameters.getDisplayNeedsBlanking()) {
            this.mDozeHost.setAodDimmingScrim(1.0f);
        }
        this.mMachine.wakeUp();
    }

    /* access modifiers changed from: public */
    private void onProximityFar(boolean z) {
        if (this.mMachine.isExecutingTransition()) {
            Log.w("DozeTriggers", "onProximityFar called during transition. Ignoring sensor response.");
            return;
        }
        boolean z2 = !z;
        DozeMachine.State state = this.mMachine.getState();
        boolean z3 = false;
        boolean z4 = state == DozeMachine.State.DOZE_AOD_PAUSED;
        boolean z5 = state == DozeMachine.State.DOZE_AOD_PAUSING;
        if (state == DozeMachine.State.DOZE_AOD) {
            z3 = true;
        }
        if (state == DozeMachine.State.DOZE_PULSING || state == DozeMachine.State.DOZE_PULSING_BRIGHT) {
            if (DEBUG) {
                Log.i("DozeTriggers", "Prox changed, ignore touch = " + z2);
            }
            this.mDozeHost.onIgnoreTouchWhilePulsing(z2);
        }
        if (z && (z4 || z5)) {
            if (DEBUG) {
                Log.i("DozeTriggers", "Prox FAR, unpausing AOD");
            }
            this.mMachine.requestState(DozeMachine.State.DOZE_AOD);
        } else if (z2 && z3) {
            if (DEBUG) {
                Log.i("DozeTriggers", "Prox NEAR, pausing AOD");
            }
            this.mMachine.requestState(DozeMachine.State.DOZE_AOD_PAUSING);
        }
    }

    private void onWakeScreen(boolean z, DozeMachine.State state) {
        this.mDozeLog.traceWakeDisplay(z);
        sWakeDisplaySensorState = z;
        boolean z2 = true;
        if (z) {
            proximityCheckThenCall(new Consumer(state) {
                /* class com.android.systemui.doze.$$Lambda$DozeTriggers$HZx5UzHarvs5L6DXQmhvvZFRQ */
                public final /* synthetic */ DozeMachine.State f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    DozeTriggers.this.lambda$onWakeScreen$2$DozeTriggers(this.f$1, (Boolean) obj);
                }
            }, true, 7);
            return;
        }
        boolean z3 = state == DozeMachine.State.DOZE_AOD_PAUSED;
        if (state != DozeMachine.State.DOZE_AOD_PAUSING) {
            z2 = false;
        }
        if (!z2 && !z3) {
            this.mMachine.requestState(DozeMachine.State.DOZE);
            this.mMetricsLogger.write(new LogMaker(223).setType(2).setSubtype(7));
        }
    }

    /* access modifiers changed from: public */
    /* access modifiers changed from: private */
    /* renamed from: lambda$onWakeScreen$2 */
    public /* synthetic */ void lambda$onWakeScreen$2$DozeTriggers(DozeMachine.State state, Boolean bool) {
        if ((bool == null || !bool.booleanValue()) && state == DozeMachine.State.DOZE) {
            this.mMachine.requestState(DozeMachine.State.DOZE_AOD);
            this.mMetricsLogger.write(new LogMaker(223).setType(1).setSubtype(7));
        }
    }

    /* renamed from: com.android.systemui.doze.DozeTriggers$2 */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$android$systemui$doze$DozeMachine$State;

        /* JADX WARNING: Can't wrap try/catch for region: R(20:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|(3:19|20|22)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(22:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|22) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x0049 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0054 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:17:0x0060 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:19:0x006c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
            // Method dump skipped, instructions count: 121
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.doze.DozeTriggers.AnonymousClass2.<clinit>():void");
        }
    }

    @Override // com.android.systemui.doze.DozeMachine.Part
    public void transitionTo(DozeMachine.State state, DozeMachine.State state2) {
        switch (AnonymousClass2.$SwitchMap$com$android$systemui$doze$DozeMachine$State[state2.ordinal()]) {
            case 1:
                this.mBroadcastReceiver.register(this.mBroadcastDispatcher);
                this.mDozeHost.addCallback(this.mHostCallback);
                this.mDockManager.addListener(this.mDockEventListener);
                this.mDozeSensors.requestTemporaryDisable();
                checkTriggersAtInit();
                return;
            case 2:
            case 3:
                this.mDozeSensors.setProxListening(state2 != DozeMachine.State.DOZE);
                this.mDozeSensors.setListening(true);
                this.mDozeSensors.setPaused(false);
                if (state2 == DozeMachine.State.DOZE_AOD && !sWakeDisplaySensorState) {
                    onWakeScreen(false, state2);
                    return;
                }
                return;
            case 4:
            case 5:
                this.mDozeSensors.setProxListening(true);
                this.mDozeSensors.setPaused(true);
                return;
            case 6:
            case 7:
            case 8:
                this.mDozeSensors.setTouchscreenSensorsListening(false);
                this.mDozeSensors.setProxListening(true);
                this.mDozeSensors.setPaused(false);
                return;
            case 9:
                this.mDozeSensors.requestTemporaryDisable();
                this.mDozeSensors.updateListening();
                return;
            case 10:
                this.mBroadcastReceiver.unregister(this.mBroadcastDispatcher);
                this.mDozeHost.removeCallback(this.mHostCallback);
                this.mDockManager.removeListener(this.mDockEventListener);
                this.mDozeSensors.setListening(false);
                this.mDozeSensors.setProxListening(false);
                return;
            default:
                return;
        }
    }

    private void checkTriggersAtInit() {
        if (this.mUiModeManager.getCurrentModeType() == 3 || this.mDozeHost.isBlockingDoze() || !this.mDozeHost.isProvisioned()) {
            this.mMachine.requestState(DozeMachine.State.FINISH);
        }
    }

    private void requestPulse(int i, boolean z, Runnable runnable) {
        Assert.isMainThread();
        this.mDozeHost.extendPulse(i);
        if (this.mMachine.getState() == DozeMachine.State.DOZE_PULSING && i == 8) {
            this.mMachine.requestState(DozeMachine.State.DOZE_PULSING_BRIGHT);
        } else if (this.mPulsePending || !this.mAllowPulseTriggers || !canPulse()) {
            if (this.mAllowPulseTriggers) {
                this.mDozeLog.tracePulseDropped(this.mPulsePending, this.mMachine.getState(), this.mDozeHost.isPulsingBlocked());
            }
            runIfNotNull(runnable);
        } else {
            boolean z2 = true;
            this.mPulsePending = true;
            $$Lambda$DozeTriggers$7efrn9gYOB_Pbk9skV2oR0AOE r1 = new Consumer(runnable, i) {
                /* class com.android.systemui.doze.$$Lambda$DozeTriggers$7efrn9gYOB_Pbk9skV2oR0AOE */
                public final /* synthetic */ Runnable f$1;
                public final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    DozeTriggers.this.lambda$requestPulse$3$DozeTriggers(this.f$1, this.f$2, (Boolean) obj);
                }
            };
            if (this.mDozeParameters.getProxCheckBeforePulse() && !z) {
                z2 = false;
            }
            proximityCheckThenCall(r1, z2, i);
            this.mMetricsLogger.write(new LogMaker(223).setType(6).setSubtype(i));
            Optional ofNullable = Optional.ofNullable(DozingUpdateUiEvent.fromReason(i));
            UiEventLogger uiEventLogger = UI_EVENT_LOGGER;
            Objects.requireNonNull(uiEventLogger);
            ofNullable.ifPresent(new Consumer(uiEventLogger) {
                /* class com.android.systemui.doze.$$Lambda$vBVHjIDgps_phZpQ4QNJ6P1upak */
                public final /* synthetic */ UiEventLogger f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    this.f$0.log((DozeTriggers.DozingUpdateUiEvent) obj);
                }
            });
        }
    }

    /* access modifiers changed from: public */
    /* access modifiers changed from: private */
    /* renamed from: lambda$requestPulse$3 */
    public /* synthetic */ void lambda$requestPulse$3$DozeTriggers(Runnable runnable, int i, Boolean bool) {
        if (bool == null || !bool.booleanValue()) {
            continuePulseRequest(i);
            return;
        }
        this.mDozeLog.tracePulseDropped("inPocket");
        this.mPulsePending = false;
        runIfNotNull(runnable);
    }

    private boolean canPulse() {
        return this.mMachine.getState() == DozeMachine.State.DOZE || this.mMachine.getState() == DozeMachine.State.DOZE_AOD || this.mMachine.getState() == DozeMachine.State.DOZE_AOD_DOCKED;
    }

    private void continuePulseRequest(int i) {
        this.mPulsePending = false;
        if (this.mDozeHost.isPulsingBlocked() || !canPulse()) {
            this.mDozeLog.tracePulseDropped(this.mPulsePending, this.mMachine.getState(), this.mDozeHost.isPulsingBlocked());
        } else {
            this.mMachine.requestPulse(i);
        }
    }

    @Override // com.android.systemui.doze.DozeMachine.Part
    public void dump(PrintWriter printWriter) {
        printWriter.print(" notificationPulseTime=");
        printWriter.println(Formatter.formatShortElapsedTime(this.mContext, this.mNotificationPulseTime));
        printWriter.println(" pulsePending=" + this.mPulsePending);
        printWriter.println("DozeSensors:");
        this.mDozeSensors.dump(printWriter);
    }

    /* access modifiers changed from: private */
    public class TriggerReceiver extends BroadcastReceiver {
        private boolean mRegistered;

        private TriggerReceiver() {
            DozeTriggers.this = r1;
        }

        public void onReceive(Context context, Intent intent) {
            if ("com.android.systemui.doze.pulse".equals(intent.getAction())) {
                if (DozeMachine.DEBUG) {
                    Log.d("DozeTriggers", "Received pulse intent");
                }
                DozeTriggers.this.requestPulse(0, false, null);
            }
            if (UiModeManager.ACTION_ENTER_CAR_MODE.equals(intent.getAction())) {
                DozeTriggers.this.mMachine.requestState(DozeMachine.State.FINISH);
            }
            if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                DozeTriggers.this.mDozeSensors.onUserSwitched();
            }
        }

        public void register(BroadcastDispatcher broadcastDispatcher) {
            if (!this.mRegistered) {
                IntentFilter intentFilter = new IntentFilter("com.android.systemui.doze.pulse");
                intentFilter.addAction(UiModeManager.ACTION_ENTER_CAR_MODE);
                intentFilter.addAction("android.intent.action.USER_SWITCHED");
                broadcastDispatcher.registerReceiver(this, intentFilter);
                this.mRegistered = true;
            }
        }

        public void unregister(BroadcastDispatcher broadcastDispatcher) {
            if (this.mRegistered) {
                broadcastDispatcher.unregisterReceiver(this);
                this.mRegistered = false;
            }
        }
    }

    /* access modifiers changed from: private */
    public class DockEventListener implements DockManager.DockEventListener {
        private DockEventListener(DozeTriggers dozeTriggers) {
        }
    }
}
