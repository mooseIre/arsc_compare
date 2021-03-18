package com.android.systemui.doze;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.provider.Settings;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.doze.DozeMachine;

public class DozeScreenBrightness extends BroadcastReceiver implements DozeMachine.Part, SensorEventListener {
    private static final boolean DEBUG_AOD_BRIGHTNESS = SystemProperties.getBoolean("debug.aod_brightness", false);
    private final BroadcastDispatcher mBroadcastDispatcher;
    private final Context mContext;
    private int mDebugBrightnessBucket;
    private final boolean mDebuggable;
    private int mDefaultDozeBrightness;
    private final DozeHost mDozeHost;
    private final DozeMachine.Service mDozeService;
    private final Handler mHandler;
    private int mLastSensorValue;
    private final Sensor mLightSensor;
    private boolean mPaused;
    private boolean mRegistered;
    private boolean mScreenOff;
    private final SensorManager mSensorManager;
    private final int[] mSensorToBrightness;
    private final int[] mSensorToScrimOpacity;

    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @VisibleForTesting
    public DozeScreenBrightness(Context context, DozeMachine.Service service, SensorManager sensorManager, Sensor sensor, BroadcastDispatcher broadcastDispatcher, DozeHost dozeHost, Handler handler, int i, int[] iArr, int[] iArr2, boolean z) {
        this.mPaused = false;
        this.mScreenOff = false;
        this.mLastSensorValue = -1;
        this.mDebugBrightnessBucket = -1;
        this.mContext = context;
        this.mDozeService = service;
        this.mSensorManager = sensorManager;
        this.mLightSensor = sensor;
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mDozeHost = dozeHost;
        this.mHandler = handler;
        this.mDebuggable = z;
        this.mDefaultDozeBrightness = i;
        this.mSensorToBrightness = iArr;
        this.mSensorToScrimOpacity = iArr2;
        if (z) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("com.android.systemui.doze.AOD_BRIGHTNESS");
            this.mBroadcastDispatcher.registerReceiverWithHandler(this, intentFilter, handler, UserHandle.ALL);
        }
    }

    public DozeScreenBrightness(Context context, DozeMachine.Service service, SensorManager sensorManager, Sensor sensor, BroadcastDispatcher broadcastDispatcher, DozeHost dozeHost, Handler handler, AlwaysOnDisplayPolicy alwaysOnDisplayPolicy) {
        this(context, service, sensorManager, sensor, broadcastDispatcher, dozeHost, handler, context.getResources().getInteger(17694890), alwaysOnDisplayPolicy.screenBrightnessArray, alwaysOnDisplayPolicy.dimmingScrimArray, DEBUG_AOD_BRIGHTNESS);
    }

    /* renamed from: com.android.systemui.doze.DozeScreenBrightness$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$systemui$doze$DozeMachine$State;

        /* JADX WARNING: Can't wrap try/catch for region: R(14:0|1|2|3|4|5|6|7|8|9|10|11|12|14) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
                com.android.systemui.doze.DozeMachine$State[] r0 = com.android.systemui.doze.DozeMachine.State.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                com.android.systemui.doze.DozeScreenBrightness.AnonymousClass1.$SwitchMap$com$android$systemui$doze$DozeMachine$State = r0
                com.android.systemui.doze.DozeMachine$State r1 = com.android.systemui.doze.DozeMachine.State.INITIALIZED     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = com.android.systemui.doze.DozeScreenBrightness.AnonymousClass1.$SwitchMap$com$android$systemui$doze$DozeMachine$State     // Catch:{ NoSuchFieldError -> 0x001d }
                com.android.systemui.doze.DozeMachine$State r1 = com.android.systemui.doze.DozeMachine.State.DOZE_AOD     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = com.android.systemui.doze.DozeScreenBrightness.AnonymousClass1.$SwitchMap$com$android$systemui$doze$DozeMachine$State     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.android.systemui.doze.DozeMachine$State r1 = com.android.systemui.doze.DozeMachine.State.DOZE_REQUEST_PULSE     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = com.android.systemui.doze.DozeScreenBrightness.AnonymousClass1.$SwitchMap$com$android$systemui$doze$DozeMachine$State     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.android.systemui.doze.DozeMachine$State r1 = com.android.systemui.doze.DozeMachine.State.DOZE_AOD_DOCKED     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = com.android.systemui.doze.DozeScreenBrightness.AnonymousClass1.$SwitchMap$com$android$systemui$doze$DozeMachine$State     // Catch:{ NoSuchFieldError -> 0x003e }
                com.android.systemui.doze.DozeMachine$State r1 = com.android.systemui.doze.DozeMachine.State.DOZE     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = com.android.systemui.doze.DozeScreenBrightness.AnonymousClass1.$SwitchMap$com$android$systemui$doze$DozeMachine$State     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.android.systemui.doze.DozeMachine$State r1 = com.android.systemui.doze.DozeMachine.State.FINISH     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.doze.DozeScreenBrightness.AnonymousClass1.<clinit>():void");
        }
    }

    @Override // com.android.systemui.doze.DozeMachine.Part
    public void transitionTo(DozeMachine.State state, DozeMachine.State state2) {
        boolean z = false;
        switch (AnonymousClass1.$SwitchMap$com$android$systemui$doze$DozeMachine$State[state2.ordinal()]) {
            case 1:
                resetBrightnessToDefault();
                break;
            case 2:
            case 3:
            case 4:
                setLightSensorEnabled(true);
                break;
            case 5:
                setLightSensorEnabled(false);
                resetBrightnessToDefault();
                break;
            case 6:
                onDestroy();
                break;
        }
        if (state2 != DozeMachine.State.FINISH) {
            setScreenOff(state2 == DozeMachine.State.DOZE);
            if (state2 == DozeMachine.State.DOZE_AOD_PAUSED) {
                z = true;
            }
            setPaused(z);
        }
    }

    private void onDestroy() {
        setLightSensorEnabled(false);
        if (this.mDebuggable) {
            this.mBroadcastDispatcher.unregisterReceiver(this);
        }
    }

    public void onSensorChanged(SensorEvent sensorEvent) {
        Trace.beginSection("DozeScreenBrightness.onSensorChanged" + sensorEvent.values[0]);
        try {
            if (this.mRegistered) {
                this.mLastSensorValue = (int) sensorEvent.values[0];
                updateBrightnessAndReady(false);
            }
        } finally {
            Trace.endSection();
        }
    }

    private void updateBrightnessAndReady(boolean z) {
        int i = -1;
        if (z || this.mRegistered || this.mDebugBrightnessBucket != -1) {
            int i2 = this.mDebugBrightnessBucket;
            if (i2 == -1) {
                i2 = this.mLastSensorValue;
            }
            int computeBrightness = computeBrightness(i2);
            boolean z2 = computeBrightness > 0;
            if (z2) {
                this.mDozeService.setDozeScreenBrightness(clampToUserSetting(computeBrightness));
            }
            if (this.mLightSensor == null) {
                i = 0;
            } else if (z2) {
                i = computeScrimOpacity(i2);
            }
            if (i >= 0) {
                this.mDozeHost.setAodDimmingScrim(((float) i) / 255.0f);
            }
        }
    }

    private int computeScrimOpacity(int i) {
        if (i < 0) {
            return -1;
        }
        int[] iArr = this.mSensorToScrimOpacity;
        if (i >= iArr.length) {
            return -1;
        }
        return iArr[i];
    }

    private int computeBrightness(int i) {
        if (i < 0) {
            return -1;
        }
        int[] iArr = this.mSensorToBrightness;
        if (i >= iArr.length) {
            return -1;
        }
        return iArr[i];
    }

    private void resetBrightnessToDefault() {
        this.mDozeService.setDozeScreenBrightness(clampToUserSetting(this.mDefaultDozeBrightness));
        this.mDozeHost.setAodDimmingScrim(0.0f);
    }

    private int clampToUserSetting(int i) {
        return Math.min(i, Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness", Integer.MAX_VALUE, -2));
    }

    private void setLightSensorEnabled(boolean z) {
        Sensor sensor;
        if (z && !this.mRegistered && (sensor = this.mLightSensor) != null) {
            this.mRegistered = this.mSensorManager.registerListener(this, sensor, 3, this.mHandler);
            this.mLastSensorValue = -1;
        } else if (!z && this.mRegistered) {
            this.mSensorManager.unregisterListener(this);
            this.mRegistered = false;
            this.mLastSensorValue = -1;
        }
    }

    private void setPaused(boolean z) {
        if (this.mPaused != z) {
            this.mPaused = z;
            updateBrightnessAndReady(false);
        }
    }

    private void setScreenOff(boolean z) {
        if (this.mScreenOff != z) {
            this.mScreenOff = z;
            updateBrightnessAndReady(true);
        }
    }

    public void onReceive(Context context, Intent intent) {
        this.mDebugBrightnessBucket = intent.getIntExtra("brightness_bucket", -1);
        updateBrightnessAndReady(false);
    }
}
