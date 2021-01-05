package com.android.systemui.doze;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.hardware.display.AmbientDisplayConfiguration;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.logging.UiEventLoggerImpl;
import com.android.systemui.doze.DozeSensors;
import com.android.systemui.plugins.SensorManagerPlugin;
import com.android.systemui.statusbar.phone.DozeParameters;
import com.android.systemui.util.sensors.AsyncSensorManager;
import com.android.systemui.util.sensors.ProximitySensor;
import com.android.systemui.util.wakelock.WakeLock;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.function.Consumer;

public class DozeSensors {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = DozeService.DEBUG;
    /* access modifiers changed from: private */
    public static final UiEventLogger UI_EVENT_LOGGER = new UiEventLoggerImpl();
    /* access modifiers changed from: private */
    public final Callback mCallback;
    /* access modifiers changed from: private */
    public final AmbientDisplayConfiguration mConfig;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public long mDebounceFrom;
    /* access modifiers changed from: private */
    public final Handler mHandler = new Handler();
    private boolean mListening;
    private boolean mPaused;
    private final Consumer<Boolean> mProxCallback;
    private final ProximitySensor mProximitySensor;
    /* access modifiers changed from: private */
    public final ContentResolver mResolver;
    /* access modifiers changed from: private */
    public final AsyncSensorManager mSensorManager;
    protected TriggerSensor[] mSensors;
    private boolean mSettingRegistered;
    /* access modifiers changed from: private */
    public final ContentObserver mSettingsObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean z, Collection<Uri> collection, int i, int i2) {
            if (i2 == ActivityManager.getCurrentUser()) {
                for (TriggerSensor updateListening : DozeSensors.this.mSensors) {
                    updateListening.updateListening();
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public final WakeLock mWakeLock;

    public interface Callback {
        void onSensorPulse(int i, float f, float f2, float[] fArr);
    }

    public enum DozeSensorsUiEvent implements UiEventLogger.UiEventEnum {
        ACTION_AMBIENT_GESTURE_PICKUP(459);
        
        private final int mId;

        private DozeSensorsUiEvent(int i) {
            this.mId = i;
        }

        public int getId() {
            return this.mId;
        }
    }

    public DozeSensors(Context context, AlarmManager alarmManager, AsyncSensorManager asyncSensorManager, DozeParameters dozeParameters, AmbientDisplayConfiguration ambientDisplayConfiguration, WakeLock wakeLock, Callback callback, Consumer<Boolean> consumer, DozeLog dozeLog, ProximitySensor proximitySensor) {
        this.mContext = context;
        this.mSensorManager = asyncSensorManager;
        this.mConfig = ambientDisplayConfiguration;
        this.mWakeLock = wakeLock;
        this.mProxCallback = consumer;
        this.mResolver = context.getContentResolver();
        this.mCallback = callback;
        this.mProximitySensor = proximitySensor;
        boolean alwaysOnEnabled = this.mConfig.alwaysOnEnabled(-2);
        TriggerSensor[] triggerSensorArr = new TriggerSensor[7];
        triggerSensorArr[0] = new TriggerSensor(this, this.mSensorManager.getDefaultSensor(17), (String) null, dozeParameters.getPulseOnSigMotion(), 2, false, false, dozeLog);
        triggerSensorArr[1] = new TriggerSensor(this.mSensorManager.getDefaultSensor(25), "doze_pulse_on_pick_up", true, ambientDisplayConfiguration.dozePickupSensorAvailable(), 3, false, false, false, dozeLog);
        DozeLog dozeLog2 = dozeLog;
        triggerSensorArr[2] = new TriggerSensor(this, findSensorWithType(ambientDisplayConfiguration.doubleTapSensorType()), "doze_pulse_on_double_tap", true, 4, dozeParameters.doubleTapReportsTouchCoordinates(), true, dozeLog2);
        triggerSensorArr[3] = new TriggerSensor(this, findSensorWithType(ambientDisplayConfiguration.tapSensorType()), "doze_tap_gesture", true, 9, false, true, dozeLog2);
        triggerSensorArr[4] = new TriggerSensor(this, findSensorWithType(ambientDisplayConfiguration.longPressSensorType()), "doze_pulse_on_long_press", false, true, 5, true, true, dozeLog);
        triggerSensorArr[5] = new PluginSensor(this, new SensorManagerPlugin.Sensor(2), "doze_wake_display_gesture", this.mConfig.wakeScreenGestureAvailable() && alwaysOnEnabled, 7, false, false, dozeLog);
        triggerSensorArr[6] = new PluginSensor(this, new SensorManagerPlugin.Sensor(1), "doze_wake_screen_gesture", this.mConfig.wakeScreenGestureAvailable(), 8, false, false, this.mConfig.getWakeLockScreenDebounce(), dozeLog);
        this.mSensors = triggerSensorArr;
        setProxListening(false);
        this.mProximitySensor.register(new ProximitySensor.ProximitySensorListener() {
            public final void onSensorEvent(ProximitySensor.ProximityEvent proximityEvent) {
                DozeSensors.this.lambda$new$0$DozeSensors(proximityEvent);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$DozeSensors(ProximitySensor.ProximityEvent proximityEvent) {
        if (proximityEvent != null) {
            this.mProxCallback.accept(Boolean.valueOf(!proximityEvent.getNear()));
        }
    }

    public void destroy() {
        for (TriggerSensor listening : this.mSensors) {
            listening.setListening(false);
        }
        this.mProximitySensor.pause();
    }

    public void requestTemporaryDisable() {
        this.mDebounceFrom = SystemClock.uptimeMillis();
    }

    private Sensor findSensorWithType(String str) {
        return findSensorWithType(this.mSensorManager, str);
    }

    static Sensor findSensorWithType(SensorManager sensorManager, String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        for (Sensor next : sensorManager.getSensorList(-1)) {
            if (str.equals(next.getStringType())) {
                return next;
            }
        }
        return null;
    }

    public void setListening(boolean z) {
        if (this.mListening != z) {
            this.mListening = z;
            updateListening();
        }
    }

    public void setPaused(boolean z) {
        if (this.mPaused != z) {
            this.mPaused = z;
            updateListening();
        }
    }

    public void updateListening() {
        boolean z = false;
        for (TriggerSensor listening : this.mSensors) {
            listening.setListening(this.mListening);
            if (this.mListening) {
                z = true;
            }
        }
        if (!z) {
            this.mResolver.unregisterContentObserver(this.mSettingsObserver);
        } else if (!this.mSettingRegistered) {
            for (TriggerSensor registerSettingsObserver : this.mSensors) {
                registerSettingsObserver.registerSettingsObserver(this.mSettingsObserver);
            }
        }
        this.mSettingRegistered = z;
    }

    public void setTouchscreenSensorsListening(boolean z) {
        for (TriggerSensor triggerSensor : this.mSensors) {
            if (triggerSensor.mRequiresTouchscreen) {
                triggerSensor.setListening(z);
            }
        }
    }

    public void onUserSwitched() {
        for (TriggerSensor updateListening : this.mSensors) {
            updateListening.updateListening();
        }
    }

    public void setProxListening(boolean z) {
        if (this.mProximitySensor.isRegistered() && z) {
            this.mProximitySensor.alertListeners();
        } else if (z) {
            this.mProximitySensor.resume();
        } else {
            this.mProximitySensor.pause();
        }
    }

    public void dump(PrintWriter printWriter) {
        for (TriggerSensor triggerSensor : this.mSensors) {
            printWriter.println("  Sensor: " + triggerSensor.toString());
        }
        printWriter.println("  ProxSensor: " + this.mProximitySensor.toString());
    }

    public Boolean isProximityCurrentlyNear() {
        return this.mProximitySensor.isNear();
    }

    class TriggerSensor extends TriggerEventListener {
        final boolean mConfigured;
        protected boolean mDisabled;
        protected final DozeLog mDozeLog;
        protected boolean mIgnoresSetting;
        final int mPulseReason;
        protected boolean mRegistered;
        private final boolean mReportsTouchCoordinates;
        protected boolean mRequested;
        /* access modifiers changed from: private */
        public final boolean mRequiresTouchscreen;
        final Sensor mSensor;
        private final String mSetting;
        private final boolean mSettingDefault;

        public TriggerSensor(DozeSensors dozeSensors, Sensor sensor, String str, boolean z, int i, boolean z2, boolean z3, DozeLog dozeLog) {
            this(dozeSensors, sensor, str, true, z, i, z2, z3, dozeLog);
        }

        public TriggerSensor(DozeSensors dozeSensors, Sensor sensor, String str, boolean z, boolean z2, int i, boolean z3, boolean z4, DozeLog dozeLog) {
            this(sensor, str, z, z2, i, z3, z4, false, dozeLog);
        }

        private TriggerSensor(Sensor sensor, String str, boolean z, boolean z2, int i, boolean z3, boolean z4, boolean z5, DozeLog dozeLog) {
            this.mSensor = sensor;
            this.mSetting = str;
            this.mSettingDefault = z;
            this.mConfigured = z2;
            this.mPulseReason = i;
            this.mReportsTouchCoordinates = z3;
            this.mRequiresTouchscreen = z4;
            this.mIgnoresSetting = z5;
            this.mDozeLog = dozeLog;
        }

        public void setListening(boolean z) {
            if (this.mRequested != z) {
                this.mRequested = z;
                updateListening();
            }
        }

        public void updateListening() {
            if (this.mConfigured && this.mSensor != null) {
                if (this.mRequested && !this.mDisabled && ((enabledBySetting() || this.mIgnoresSetting) && !this.mRegistered)) {
                    this.mRegistered = DozeSensors.this.mSensorManager.requestTriggerSensor(this, this.mSensor);
                    if (DozeSensors.DEBUG) {
                        Log.d("DozeSensors", "requestTriggerSensor " + this.mRegistered);
                    }
                } else if (this.mRegistered) {
                    boolean cancelTriggerSensor = DozeSensors.this.mSensorManager.cancelTriggerSensor(this, this.mSensor);
                    if (DozeSensors.DEBUG) {
                        Log.d("DozeSensors", "cancelTriggerSensor " + cancelTriggerSensor);
                    }
                    this.mRegistered = false;
                }
            }
        }

        /* access modifiers changed from: protected */
        public boolean enabledBySetting() {
            if (!DozeSensors.this.mConfig.enabled(-2)) {
                return false;
            }
            if (TextUtils.isEmpty(this.mSetting)) {
                return true;
            }
            if (Settings.Secure.getIntForUser(DozeSensors.this.mResolver, this.mSetting, this.mSettingDefault ? 1 : 0, -2) != 0) {
                return true;
            }
            return false;
        }

        public String toString() {
            return "{mRegistered=" + this.mRegistered + ", mRequested=" + this.mRequested + ", mDisabled=" + this.mDisabled + ", mConfigured=" + this.mConfigured + ", mIgnoresSetting=" + this.mIgnoresSetting + ", mSensor=" + this.mSensor + "}";
        }

        public void onTrigger(TriggerEvent triggerEvent) {
            this.mDozeLog.traceSensor(this.mPulseReason);
            DozeSensors.this.mHandler.post(DozeSensors.this.mWakeLock.wrap(new Runnable(triggerEvent) {
                public final /* synthetic */ TriggerEvent f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    DozeSensors.TriggerSensor.this.lambda$onTrigger$0$DozeSensors$TriggerSensor(this.f$1);
                }
            }));
        }

        /* access modifiers changed from: private */
        /* JADX WARNING: Removed duplicated region for block: B:16:0x006c  */
        /* JADX WARNING: Removed duplicated region for block: B:18:? A[RETURN, SYNTHETIC] */
        /* renamed from: lambda$onTrigger$0 */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public /* synthetic */ void lambda$onTrigger$0$DozeSensors$TriggerSensor(android.hardware.TriggerEvent r6) {
            /*
                r5 = this;
                boolean r0 = com.android.systemui.doze.DozeSensors.DEBUG
                if (r0 == 0) goto L_0x0020
                java.lang.StringBuilder r0 = new java.lang.StringBuilder
                r0.<init>()
                java.lang.String r1 = "onTrigger: "
                r0.append(r1)
                java.lang.String r1 = r5.triggerEventToString(r6)
                r0.append(r1)
                java.lang.String r0 = r0.toString()
                java.lang.String r1 = "DozeSensors"
                android.util.Log.d(r1, r0)
            L_0x0020:
                android.hardware.Sensor r0 = r5.mSensor
                r1 = 0
                if (r0 == 0) goto L_0x0046
                int r0 = r0.getType()
                r2 = 25
                if (r0 != r2) goto L_0x0046
                float[] r0 = r6.values
                r0 = r0[r1]
                int r0 = (int) r0
                com.android.systemui.doze.DozeSensors r2 = com.android.systemui.doze.DozeSensors.this
                android.content.Context r2 = r2.mContext
                r3 = 411(0x19b, float:5.76E-43)
                com.android.internal.logging.MetricsLogger.action(r2, r3, r0)
                com.android.internal.logging.UiEventLogger r0 = com.android.systemui.doze.DozeSensors.UI_EVENT_LOGGER
                com.android.systemui.doze.DozeSensors$DozeSensorsUiEvent r2 = com.android.systemui.doze.DozeSensors.DozeSensorsUiEvent.ACTION_AMBIENT_GESTURE_PICKUP
                r0.log(r2)
            L_0x0046:
                r5.mRegistered = r1
                boolean r0 = r5.mReportsTouchCoordinates
                r2 = -1082130432(0xffffffffbf800000, float:-1.0)
                if (r0 == 0) goto L_0x005a
                float[] r0 = r6.values
                int r3 = r0.length
                r4 = 2
                if (r3 < r4) goto L_0x005a
                r2 = r0[r1]
                r1 = 1
                r0 = r0[r1]
                goto L_0x005b
            L_0x005a:
                r0 = r2
            L_0x005b:
                com.android.systemui.doze.DozeSensors r1 = com.android.systemui.doze.DozeSensors.this
                com.android.systemui.doze.DozeSensors$Callback r1 = r1.mCallback
                int r3 = r5.mPulseReason
                float[] r6 = r6.values
                r1.onSensorPulse(r3, r2, r0, r6)
                boolean r6 = r5.mRegistered
                if (r6 != 0) goto L_0x006f
                r5.updateListening()
            L_0x006f:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.doze.DozeSensors.TriggerSensor.lambda$onTrigger$0$DozeSensors$TriggerSensor(android.hardware.TriggerEvent):void");
        }

        public void registerSettingsObserver(ContentObserver contentObserver) {
            if (this.mConfigured && !TextUtils.isEmpty(this.mSetting)) {
                DozeSensors.this.mResolver.registerContentObserver(Settings.Secure.getUriFor(this.mSetting), false, DozeSensors.this.mSettingsObserver, -1);
            }
        }

        /* access modifiers changed from: protected */
        public String triggerEventToString(TriggerEvent triggerEvent) {
            if (triggerEvent == null) {
                return null;
            }
            StringBuilder sb = new StringBuilder("SensorEvent[");
            sb.append(triggerEvent.timestamp);
            sb.append(',');
            sb.append(triggerEvent.sensor.getName());
            if (triggerEvent.values != null) {
                for (float append : triggerEvent.values) {
                    sb.append(',');
                    sb.append(append);
                }
            }
            sb.append(']');
            return sb.toString();
        }
    }

    class PluginSensor extends TriggerSensor implements SensorManagerPlugin.SensorEventListener {
        private long mDebounce;
        final SensorManagerPlugin.Sensor mPluginSensor;
        final /* synthetic */ DozeSensors this$0;

        PluginSensor(DozeSensors dozeSensors, SensorManagerPlugin.Sensor sensor, String str, boolean z, int i, boolean z2, boolean z3, DozeLog dozeLog) {
            this(dozeSensors, sensor, str, z, i, z2, z3, 0, dozeLog);
        }

        /* JADX WARNING: Illegal instructions before constructor call */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        PluginSensor(com.android.systemui.doze.DozeSensors r11, com.android.systemui.plugins.SensorManagerPlugin.Sensor r12, java.lang.String r13, boolean r14, int r15, boolean r16, boolean r17, long r18, com.android.systemui.doze.DozeLog r20) {
            /*
                r10 = this;
                r9 = r10
                r1 = r11
                r9.this$0 = r1
                r2 = 0
                r0 = r10
                r3 = r13
                r4 = r14
                r5 = r15
                r6 = r16
                r7 = r17
                r8 = r20
                r0.<init>(r1, r2, r3, r4, r5, r6, r7, r8)
                r0 = r12
                r9.mPluginSensor = r0
                r0 = r18
                r9.mDebounce = r0
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.doze.DozeSensors.PluginSensor.<init>(com.android.systemui.doze.DozeSensors, com.android.systemui.plugins.SensorManagerPlugin$Sensor, java.lang.String, boolean, int, boolean, boolean, long, com.android.systemui.doze.DozeLog):void");
        }

        public void updateListening() {
            if (this.mConfigured) {
                AsyncSensorManager access$200 = this.this$0.mSensorManager;
                if (this.mRequested && !this.mDisabled && ((enabledBySetting() || this.mIgnoresSetting) && !this.mRegistered)) {
                    access$200.registerPluginListener(this.mPluginSensor, this);
                    this.mRegistered = true;
                    if (DozeSensors.DEBUG) {
                        Log.d("DozeSensors", "registerPluginListener");
                    }
                } else if (this.mRegistered) {
                    access$200.unregisterPluginListener(this.mPluginSensor, this);
                    this.mRegistered = false;
                    if (DozeSensors.DEBUG) {
                        Log.d("DozeSensors", "unregisterPluginListener");
                    }
                }
            }
        }

        public String toString() {
            return "{mRegistered=" + this.mRegistered + ", mRequested=" + this.mRequested + ", mDisabled=" + this.mDisabled + ", mConfigured=" + this.mConfigured + ", mIgnoresSetting=" + this.mIgnoresSetting + ", mSensor=" + this.mPluginSensor + "}";
        }

        private String triggerEventToString(SensorManagerPlugin.SensorEvent sensorEvent) {
            if (sensorEvent == null) {
                return null;
            }
            StringBuilder sb = new StringBuilder("PluginTriggerEvent[");
            sb.append(sensorEvent.getSensor());
            sb.append(',');
            sb.append(sensorEvent.getVendorType());
            if (sensorEvent.getValues() != null) {
                for (float append : sensorEvent.getValues()) {
                    sb.append(',');
                    sb.append(append);
                }
            }
            sb.append(']');
            return sb.toString();
        }

        public void onSensorChanged(SensorManagerPlugin.SensorEvent sensorEvent) {
            this.mDozeLog.traceSensor(this.mPulseReason);
            this.this$0.mHandler.post(this.this$0.mWakeLock.wrap(new Runnable(sensorEvent) {
                public final /* synthetic */ SensorManagerPlugin.SensorEvent f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    DozeSensors.PluginSensor.this.lambda$onSensorChanged$0$DozeSensors$PluginSensor(this.f$1);
                }
            }));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onSensorChanged$0 */
        public /* synthetic */ void lambda$onSensorChanged$0$DozeSensors$PluginSensor(SensorManagerPlugin.SensorEvent sensorEvent) {
            if (SystemClock.uptimeMillis() < this.this$0.mDebounceFrom + this.mDebounce) {
                Log.d("DozeSensors", "onSensorEvent dropped: " + triggerEventToString(sensorEvent));
                return;
            }
            if (DozeSensors.DEBUG) {
                Log.d("DozeSensors", "onSensorEvent: " + triggerEventToString(sensorEvent));
            }
            this.this$0.mCallback.onSensorPulse(this.mPulseReason, -1.0f, -1.0f, sensorEvent.getValues());
        }
    }
}
