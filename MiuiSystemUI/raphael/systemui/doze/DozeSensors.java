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
    private static final boolean DEBUG = DozeService.DEBUG;
    private static final UiEventLogger UI_EVENT_LOGGER = new UiEventLoggerImpl();
    private final Callback mCallback;
    private final AmbientDisplayConfiguration mConfig;
    private final Context mContext;
    private long mDebounceFrom;
    private final Handler mHandler = new Handler();
    private boolean mListening;
    private boolean mPaused;
    private final Consumer<Boolean> mProxCallback;
    private final ProximitySensor mProximitySensor;
    private final ContentResolver mResolver;
    private final AsyncSensorManager mSensorManager;
    protected TriggerSensor[] mSensors;
    private boolean mSettingRegistered;
    private final ContentObserver mSettingsObserver = new ContentObserver(this.mHandler) {
        /* class com.android.systemui.doze.DozeSensors.AnonymousClass1 */

        public void onChange(boolean z, Collection<Uri> collection, int i, int i2) {
            if (i2 == ActivityManager.getCurrentUser()) {
                for (TriggerSensor triggerSensor : DozeSensors.this.mSensors) {
                    triggerSensor.updateListening();
                }
            }
        }
    };
    private final WakeLock mWakeLock;

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
        triggerSensorArr[0] = new TriggerSensor(this, this.mSensorManager.getDefaultSensor(17), null, dozeParameters.getPulseOnSigMotion(), 2, false, false, dozeLog);
        triggerSensorArr[1] = new TriggerSensor(this.mSensorManager.getDefaultSensor(25), "doze_pulse_on_pick_up", true, ambientDisplayConfiguration.dozePickupSensorAvailable(), 3, false, false, false, dozeLog);
        triggerSensorArr[2] = new TriggerSensor(this, findSensorWithType(ambientDisplayConfiguration.doubleTapSensorType()), "doze_pulse_on_double_tap", true, 4, dozeParameters.doubleTapReportsTouchCoordinates(), true, dozeLog);
        triggerSensorArr[3] = new TriggerSensor(this, findSensorWithType(ambientDisplayConfiguration.tapSensorType()), "doze_tap_gesture", true, 9, false, true, dozeLog);
        triggerSensorArr[4] = new TriggerSensor(this, findSensorWithType(ambientDisplayConfiguration.longPressSensorType()), "doze_pulse_on_long_press", false, true, 5, true, true, dozeLog);
        triggerSensorArr[5] = new PluginSensor(this, new SensorManagerPlugin.Sensor(2), "doze_wake_display_gesture", this.mConfig.wakeScreenGestureAvailable() && alwaysOnEnabled, 7, false, false, dozeLog);
        triggerSensorArr[6] = new PluginSensor(new SensorManagerPlugin.Sensor(1), "doze_wake_screen_gesture", this.mConfig.wakeScreenGestureAvailable(), 8, false, false, this.mConfig.getWakeLockScreenDebounce(), dozeLog);
        this.mSensors = triggerSensorArr;
        setProxListening(false);
        this.mProximitySensor.register(new ProximitySensor.ProximitySensorListener() {
            /* class com.android.systemui.doze.$$Lambda$DozeSensors$eWcsfaBj95QArTbTaV_jJjjsPh4 */

            @Override // com.android.systemui.util.sensors.ProximitySensor.ProximitySensorListener
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
        for (TriggerSensor triggerSensor : this.mSensors) {
            triggerSensor.setListening(false);
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
        for (Sensor sensor : sensorManager.getSensorList(-1)) {
            if (str.equals(sensor.getStringType())) {
                return sensor;
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
        for (TriggerSensor triggerSensor : this.mSensors) {
            triggerSensor.setListening(this.mListening);
            if (this.mListening) {
                z = true;
            }
        }
        if (!z) {
            this.mResolver.unregisterContentObserver(this.mSettingsObserver);
        } else if (!this.mSettingRegistered) {
            for (TriggerSensor triggerSensor2 : this.mSensors) {
                triggerSensor2.registerSettingsObserver(this.mSettingsObserver);
            }
        }
        this.mSettingRegistered = z;
    }

    public void setTouchscreenSensorsListening(boolean z) {
        TriggerSensor[] triggerSensorArr = this.mSensors;
        for (TriggerSensor triggerSensor : triggerSensorArr) {
            if (triggerSensor.mRequiresTouchscreen) {
                triggerSensor.setListening(z);
            }
        }
    }

    public void onUserSwitched() {
        for (TriggerSensor triggerSensor : this.mSensors) {
            triggerSensor.updateListening();
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
        TriggerSensor[] triggerSensorArr = this.mSensors;
        for (TriggerSensor triggerSensor : triggerSensorArr) {
            printWriter.println("  Sensor: " + triggerSensor.toString());
        }
        printWriter.println("  ProxSensor: " + this.mProximitySensor.toString());
    }

    public Boolean isProximityCurrentlyNear() {
        return this.mProximitySensor.isNear();
    }

    /* access modifiers changed from: package-private */
    public class TriggerSensor extends TriggerEventListener {
        final boolean mConfigured;
        protected boolean mDisabled;
        protected final DozeLog mDozeLog;
        protected boolean mIgnoresSetting;
        final int mPulseReason;
        protected boolean mRegistered;
        private final boolean mReportsTouchCoordinates;
        protected boolean mRequested;
        private final boolean mRequiresTouchscreen;
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
                /* class com.android.systemui.doze.$$Lambda$DozeSensors$TriggerSensor$O2XJN2HKJ96bSF_1qNx6jPKeFk */
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
            // Method dump skipped, instructions count: 112
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
                for (int i = 0; i < triggerEvent.values.length; i++) {
                    sb.append(',');
                    sb.append(triggerEvent.values[i]);
                }
            }
            sb.append(']');
            return sb.toString();
        }
    }

    class PluginSensor extends TriggerSensor implements SensorManagerPlugin.SensorEventListener {
        private long mDebounce;
        final SensorManagerPlugin.Sensor mPluginSensor;

        PluginSensor(DozeSensors dozeSensors, SensorManagerPlugin.Sensor sensor, String str, boolean z, int i, boolean z2, boolean z3, DozeLog dozeLog) {
            this(sensor, str, z, i, z2, z3, 0, dozeLog);
        }

        PluginSensor(SensorManagerPlugin.Sensor sensor, String str, boolean z, int i, boolean z2, boolean z3, long j, DozeLog dozeLog) {
            super(DozeSensors.this, null, str, z, i, z2, z3, dozeLog);
            this.mPluginSensor = sensor;
            this.mDebounce = j;
        }

        @Override // com.android.systemui.doze.DozeSensors.TriggerSensor
        public void updateListening() {
            if (this.mConfigured) {
                AsyncSensorManager asyncSensorManager = DozeSensors.this.mSensorManager;
                if (this.mRequested && !this.mDisabled && ((enabledBySetting() || this.mIgnoresSetting) && !this.mRegistered)) {
                    asyncSensorManager.registerPluginListener(this.mPluginSensor, this);
                    this.mRegistered = true;
                    if (DozeSensors.DEBUG) {
                        Log.d("DozeSensors", "registerPluginListener");
                    }
                } else if (this.mRegistered) {
                    asyncSensorManager.unregisterPluginListener(this.mPluginSensor, this);
                    this.mRegistered = false;
                    if (DozeSensors.DEBUG) {
                        Log.d("DozeSensors", "unregisterPluginListener");
                    }
                }
            }
        }

        @Override // com.android.systemui.doze.DozeSensors.TriggerSensor
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
                for (int i = 0; i < sensorEvent.getValues().length; i++) {
                    sb.append(',');
                    sb.append(sensorEvent.getValues()[i]);
                }
            }
            sb.append(']');
            return sb.toString();
        }

        @Override // com.android.systemui.plugins.SensorManagerPlugin.SensorEventListener
        public void onSensorChanged(SensorManagerPlugin.SensorEvent sensorEvent) {
            this.mDozeLog.traceSensor(this.mPulseReason);
            DozeSensors.this.mHandler.post(DozeSensors.this.mWakeLock.wrap(new Runnable(sensorEvent) {
                /* class com.android.systemui.doze.$$Lambda$DozeSensors$PluginSensor$EFDqlQhDL6RwEmmtbTd8M88V_8Y */
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
            if (SystemClock.uptimeMillis() < DozeSensors.this.mDebounceFrom + this.mDebounce) {
                Log.d("DozeSensors", "onSensorEvent dropped: " + triggerEventToString(sensorEvent));
                return;
            }
            if (DozeSensors.DEBUG) {
                Log.d("DozeSensors", "onSensorEvent: " + triggerEventToString(sensorEvent));
            }
            DozeSensors.this.mCallback.onSensorPulse(this.mPulseReason, -1.0f, -1.0f, sensorEvent.getValues());
        }
    }
}
