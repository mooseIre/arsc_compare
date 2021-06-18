package com.android.keyguard.injector;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.systemui.C0008R$array;
import com.android.systemui.Dependency;
import com.android.systemui.UiOffloadThread;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.miui.systemui.SettingsObserver;
import com.miui.systemui.util.CommonUtil;
import com.miui.systemui.util.MiuiTextUtils;
import kotlin.TypeCastException;
import kotlin.collections.ArraysKt;
import kotlin.jvm.internal.Intrinsics;
import miui.os.Build;
import miui.os.SystemProperties;
import miui.util.ProximitySensorWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: KeyguardSensorInjector.kt */
public final class KeyguardSensorInjector implements SettingsObserver.Callback, WakefulnessLifecycle.Observer {
    private final int LARGE_AREA_TOUCH_SENSOR;
    private final String LARGE_AREA_TOUCH_SENSOR_NAME;
    private final String SCREEN_OFF_REASON;
    private final String SCREEN_OPEN_REASON;
    @NotNull
    private final String TAG = "KeyguardSensorInjector";
    private final int WAKEUP_AND_SLEEP_SENSOR_MTK;
    private final String WAKEUP_AND_SLEEP_SENSOR_NAME1;
    private final String WAKEUP_AND_SLEEP_SENSOR_NAME2;
    private final int WAKEUP_AND_SLEEP_SENSOR_XIAOMI;
    @NotNull
    private final Context mContext;
    private Display mDisplay;
    private final Handler mHandler;
    private boolean mIsDeviceSupportLargeAreaTouch;
    @NotNull
    private final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private final MiuiKeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback;
    @NotNull
    private final KeyguardViewMediator mKeyguardViewMediator;
    private Sensor mLargeAreaTouchSensor;
    private final SensorEventListener mLargeAreaTouchSensorListener;
    private final SensorEventListener mPickupSensorListener;
    private boolean mPickupSensorSettingsOpened;
    @NotNull
    private final PowerManager mPowerManager;
    private ProximitySensorChangeCallback mProximitySensorChangeCallback;
    private final ProximitySensorWrapper.ProximitySensorChangeListener mProximitySensorListener;
    private ProximitySensorWrapper mProximitySensorWrapper;
    private final SensorManager mSensorManager;
    private final UiOffloadThread mUiOffloadThread = ((UiOffloadThread) Dependency.get(UiOffloadThread.class));
    private final Runnable mUnregisterProximitySensorRunnable;
    private Sensor mWakeupAndSleepSensor;
    private boolean mWakeupByPickUp;
    private final boolean sIsEllipticProximity;

    /* compiled from: KeyguardSensorInjector.kt */
    public interface ProximitySensorChangeCallback {
        void onChange(boolean z);
    }

    public KeyguardSensorInjector(@NotNull Context context, @NotNull KeyguardViewMediator keyguardViewMediator, @NotNull PowerManager powerManager, @NotNull KeyguardUpdateMonitor keyguardUpdateMonitor, @NotNull WakefulnessLifecycle wakefulnessLifecycle) {
        Intrinsics.checkParameterIsNotNull(context, "mContext");
        Intrinsics.checkParameterIsNotNull(keyguardViewMediator, "mKeyguardViewMediator");
        Intrinsics.checkParameterIsNotNull(powerManager, "mPowerManager");
        Intrinsics.checkParameterIsNotNull(keyguardUpdateMonitor, "mKeyguardUpdateMonitor");
        Intrinsics.checkParameterIsNotNull(wakefulnessLifecycle, "wakefulnessLifecycle");
        this.mContext = context;
        this.mKeyguardViewMediator = keyguardViewMediator;
        this.mPowerManager = powerManager;
        this.mKeyguardUpdateMonitor = keyguardUpdateMonitor;
        Object systemService = this.mContext.getSystemService("sensor");
        if (systemService != null) {
            this.mSensorManager = (SensorManager) systemService;
            this.mHandler = new Handler(Looper.getMainLooper());
            this.SCREEN_OPEN_REASON = "keyguard_screen_on_reason";
            this.SCREEN_OFF_REASON = "keyguard_screen_off_reason";
            this.LARGE_AREA_TOUCH_SENSOR = 33171031;
            this.LARGE_AREA_TOUCH_SENSOR_NAME = "Touch Sensor";
            this.WAKEUP_AND_SLEEP_SENSOR_XIAOMI = 33171036;
            this.WAKEUP_AND_SLEEP_SENSOR_MTK = 22;
            this.WAKEUP_AND_SLEEP_SENSOR_NAME1 = "oem7 Pick Up Gesture";
            this.WAKEUP_AND_SLEEP_SENSOR_NAME2 = "pickup  Wakeup";
            this.mUnregisterProximitySensorRunnable = new KeyguardSensorInjector$mUnregisterProximitySensorRunnable$1(this);
            this.mProximitySensorListener = new KeyguardSensorInjector$mProximitySensorListener$1(this);
            this.mPickupSensorListener = new KeyguardSensorInjector$mPickupSensorListener$1(this);
            this.mLargeAreaTouchSensorListener = new KeyguardSensorInjector$mLargeAreaTouchSensorListener$1(this);
            KeyguardSensorInjector$mKeyguardUpdateMonitorCallback$1 keyguardSensorInjector$mKeyguardUpdateMonitorCallback$1 = new KeyguardSensorInjector$mKeyguardUpdateMonitorCallback$1(this);
            this.mKeyguardUpdateMonitorCallback = keyguardSensorInjector$mKeyguardUpdateMonitorCallback$1;
            this.mKeyguardUpdateMonitor.registerCallback(keyguardSensorInjector$mKeyguardUpdateMonitorCallback$1);
            wakefulnessLifecycle.addObserver(this);
            boolean z = false;
            this.sIsEllipticProximity = (SystemProperties.getBoolean("ro.vendor.audio.us.proximity", false) || SystemProperties.getBoolean("ro.audio.us.proximity", false)) ? true : z;
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type android.hardware.SensorManager");
    }

    @NotNull
    public final Context getMContext() {
        return this.mContext;
    }

    @NotNull
    public final KeyguardViewMediator getMKeyguardViewMediator() {
        return this.mKeyguardViewMediator;
    }

    @NotNull
    public final PowerManager getMPowerManager() {
        return this.mPowerManager;
    }

    @NotNull
    public final String getTAG() {
        return this.TAG;
    }

    public final void setupSensors() {
        Object systemService = this.mContext.getSystemService("window");
        if (systemService != null) {
            this.mDisplay = ((WindowManager) systemService).getDefaultDisplay();
            this.mIsDeviceSupportLargeAreaTouch = isDeviceSupportLargeAreaTouch();
            ((SettingsObserver) Dependency.get(SettingsObserver.class)).addCallbackForType(this, 1, "pick_up_gesture_wakeup_mode");
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type android.view.WindowManager");
    }

    @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
    public void onStartedWakingUp() {
        registerLargeAreaTouchSensor();
    }

    @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
    public void onStartedGoingToSleep() {
        this.mWakeupByPickUp = false;
        registerPickupSensor();
    }

    @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
    public void onFinishedGoingToSleep() {
        unregisterLargeAreaTouchSensor();
        unregisterProximitySensor();
    }

    public final void registerProximitySensor(@Nullable ProximitySensorChangeCallback proximitySensorChangeCallback) {
        if (this.mProximitySensorWrapper == null) {
            ProximitySensorWrapper proximitySensorWrapper = new ProximitySensorWrapper(this.mContext);
            this.mProximitySensorWrapper = proximitySensorWrapper;
            if (proximitySensorWrapper != null) {
                proximitySensorWrapper.registerListener(this.mProximitySensorListener);
                this.mProximitySensorChangeCallback = proximitySensorChangeCallback;
                this.mHandler.postDelayed(this.mUnregisterProximitySensorRunnable, 2000);
                return;
            }
            Intrinsics.throwNpe();
            throw null;
        }
    }

    public final void unregisterProximitySensor() {
        if (this.mProximitySensorWrapper != null) {
            this.mHandler.removeCallbacks(this.mUnregisterProximitySensorRunnable);
            ProximitySensorWrapper proximitySensorWrapper = this.mProximitySensorWrapper;
            if (proximitySensorWrapper != null) {
                proximitySensorWrapper.unregisterAllListeners();
                this.mProximitySensorWrapper = null;
                this.mProximitySensorChangeCallback = null;
                return;
            }
            Intrinsics.throwNpe();
            throw null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0036, code lost:
        if (kotlin.text.StringsKt.equals(r0, r4, true) != false) goto L_0x0038;
     */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0058  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x005c  */
    /* JADX WARNING: Removed duplicated region for block: B:30:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final void registerPickupSensor() {
        /*
        // Method dump skipped, instructions count: 103
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.injector.KeyguardSensorInjector.registerPickupSensor():void");
    }

    /* access modifiers changed from: private */
    public final void unregisterPickupSensor() {
        this.mUiOffloadThread.submit(new KeyguardSensorInjector$unregisterPickupSensor$1(this));
    }

    @Override // com.miui.systemui.SettingsObserver.Callback
    public void onContentChanged(@Nullable String str, @Nullable String str2) {
        if (Intrinsics.areEqual("pick_up_gesture_wakeup_mode", str)) {
            this.mPickupSensorSettingsOpened = MiuiTextUtils.parseBoolean(str2);
            boolean isFingerprintUnlock = ((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).isFingerprintUnlock();
            boolean isHiding = this.mKeyguardViewMediator.isHiding();
            boolean isShowing = this.mKeyguardViewMediator.isShowing();
            String str3 = this.TAG;
            Log.d(str3, " onContentChanged mHiding:" + isHiding + "  mShowing=" + isShowing + "    unlock=" + isFingerprintUnlock + "  mPickupGestureWakeupOpened:" + this.mPickupSensorSettingsOpened + "  newValue:" + str2);
            if (!isHiding && isShowing && !isFingerprintUnlock) {
                if (this.mPickupSensorSettingsOpened) {
                    registerPickupSensor();
                } else {
                    unregisterPickupSensor();
                }
            }
        }
    }

    public final void registerLargeAreaTouchSensor() {
        if (shouldRegisterLargeAreaSensor()) {
            this.mUiOffloadThread.submit(new KeyguardSensorInjector$registerLargeAreaTouchSensor$1(this));
        }
    }

    public final void unregisterLargeAreaTouchSensor() {
        if (this.mIsDeviceSupportLargeAreaTouch) {
            this.mUiOffloadThread.submit(new KeyguardSensorInjector$unregisterLargeAreaTouchSensor$1(this));
        }
    }

    private final boolean isDeviceSupportLargeAreaTouch() {
        Sensor defaultSensor;
        SensorManager sensorManager = this.mSensorManager;
        return (sensorManager == null || (defaultSensor = sensorManager.getDefaultSensor(this.LARGE_AREA_TOUCH_SENSOR)) == null || !Intrinsics.areEqual(this.LARGE_AREA_TOUCH_SENSOR_NAME, defaultSensor.getName())) ? false : true;
    }

    /* access modifiers changed from: private */
    public final boolean shouldRegisterLargeAreaSensor() {
        return this.mIsDeviceSupportLargeAreaTouch && this.mSensorManager != null && this.mLargeAreaTouchSensor == null && !this.mKeyguardViewMediator.isHiding() && this.mKeyguardViewMediator.isShowing();
    }

    public final boolean isProximitySensorDisabled() {
        return this.mSensorManager.getDefaultSensor(8) == null || this.sIsEllipticProximity;
    }

    public final boolean isSupportPickupByMTK() {
        String[] stringArray = this.mContext.getResources().getStringArray(C0008R$array.device_support_pickup_by_MTK);
        Intrinsics.checkExpressionValueIsNotNull(stringArray, "mContext.resources.getSt…ce_support_pickup_by_MTK)");
        return ArraysKt.contains(stringArray, Build.DEVICE);
    }

    public final void disableFullScreenGesture() {
        if (MiuiKeyguardUtils.isFullScreenGestureOpened()) {
            CommonUtil.updateFsgState(this.mContext, "typefrom_keyguard", this.mKeyguardViewMediator.isShowing() && !this.mKeyguardViewMediator.isOccluded() && !this.mKeyguardUpdateMonitor.isBouncerShowing());
        }
    }
}
