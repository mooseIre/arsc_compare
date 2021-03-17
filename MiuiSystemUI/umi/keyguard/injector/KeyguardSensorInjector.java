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
import kotlin.jvm.internal.Intrinsics;
import miui.os.Build;
import miui.os.SystemProperties;
import miui.util.ProximitySensorWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: KeyguardSensorInjector.kt */
public final class KeyguardSensorInjector implements SettingsObserver.Callback, WakefulnessLifecycle.Observer {
    /* access modifiers changed from: private */
    public final int LARGE_AREA_TOUCH_SENSOR;
    private final String LARGE_AREA_TOUCH_SENSOR_NAME;
    /* access modifiers changed from: private */
    public final String SCREEN_OFF_REASON;
    /* access modifiers changed from: private */
    public final String SCREEN_OPEN_REASON;
    @NotNull
    private final String TAG = "KeyguardSensorInjector";
    private final int WAKEUP_AND_SLEEP_SENSOR_MTK;
    private final String WAKEUP_AND_SLEEP_SENSOR_NAME1;
    private final String WAKEUP_AND_SLEEP_SENSOR_NAME2;
    private final int WAKEUP_AND_SLEEP_SENSOR_XIAOMI;
    @NotNull
    private final Context mContext;
    /* access modifiers changed from: private */
    public Display mDisplay;
    private final Handler mHandler;
    private boolean mIsDeviceSupportLargeAreaTouch;
    @NotNull
    private final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private final MiuiKeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback;
    @NotNull
    private final KeyguardViewMediator mKeyguardViewMediator;
    /* access modifiers changed from: private */
    public Sensor mLargeAreaTouchSensor;
    /* access modifiers changed from: private */
    public final SensorEventListener mLargeAreaTouchSensorListener;
    /* access modifiers changed from: private */
    public final SensorEventListener mPickupSensorListener;
    private boolean mPickupSensorSettingsOpened;
    @NotNull
    private final PowerManager mPowerManager;
    /* access modifiers changed from: private */
    public ProximitySensorChangeCallback mProximitySensorChangeCallback;
    private final ProximitySensorWrapper.ProximitySensorChangeListener mProximitySensorListener;
    private ProximitySensorWrapper mProximitySensorWrapper;
    /* access modifiers changed from: private */
    public final SensorManager mSensorManager;
    private final UiOffloadThread mUiOffloadThread = ((UiOffloadThread) Dependency.get(UiOffloadThread.class));
    private final Runnable mUnregisterProximitySensorRunnable;
    /* access modifiers changed from: private */
    public Sensor mWakeupAndSleepSensor;
    /* access modifiers changed from: private */
    public boolean mWakeupByPickUp;
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

    public void onStartedWakingUp() {
        registerLargeAreaTouchSensor();
    }

    public void onStartedGoingToSleep() {
        this.mWakeupByPickUp = false;
        registerPickupSensor();
    }

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
            r5 = this;
            android.hardware.Sensor r0 = r5.mWakeupAndSleepSensor
            if (r0 != 0) goto L_0x0066
            boolean r0 = r5.mPickupSensorSettingsOpened
            if (r0 != 0) goto L_0x0009
            goto L_0x0066
        L_0x0009:
            android.hardware.SensorManager r0 = r5.mSensorManager
            int r1 = r5.WAKEUP_AND_SLEEP_SENSOR_XIAOMI
            r2 = 1
            android.hardware.Sensor r0 = r0.getDefaultSensor(r1, r2)
            r5.mWakeupAndSleepSensor = r0
            r1 = 0
            if (r0 == 0) goto L_0x003a
            java.lang.String r3 = r5.WAKEUP_AND_SLEEP_SENSOR_NAME1
            r4 = 0
            if (r0 == 0) goto L_0x0021
            java.lang.String r0 = r0.getName()
            goto L_0x0022
        L_0x0021:
            r0 = r4
        L_0x0022:
            boolean r0 = kotlin.text.StringsKt__StringsJVMKt.equals(r3, r0, r2)
            if (r0 != 0) goto L_0x0038
            java.lang.String r0 = r5.WAKEUP_AND_SLEEP_SENSOR_NAME2
            android.hardware.Sensor r3 = r5.mWakeupAndSleepSensor
            if (r3 == 0) goto L_0x0032
            java.lang.String r4 = r3.getName()
        L_0x0032:
            boolean r0 = kotlin.text.StringsKt__StringsJVMKt.equals(r0, r4, r2)
            if (r0 == 0) goto L_0x003a
        L_0x0038:
            r0 = r2
            goto L_0x003b
        L_0x003a:
            r0 = r1
        L_0x003b:
            if (r0 != 0) goto L_0x005a
            java.lang.Class<com.android.keyguard.injector.KeyguardSensorInjector> r3 = com.android.keyguard.injector.KeyguardSensorInjector.class
            java.lang.Object r3 = com.android.systemui.Dependency.get(r3)
            com.android.keyguard.injector.KeyguardSensorInjector r3 = (com.android.keyguard.injector.KeyguardSensorInjector) r3
            boolean r3 = r3.isSupportPickupByMTK()
            if (r3 == 0) goto L_0x005a
            android.hardware.SensorManager r0 = r5.mSensorManager
            int r3 = r5.WAKEUP_AND_SLEEP_SENSOR_MTK
            android.hardware.Sensor r0 = r0.getDefaultSensor(r3, r2)
            r5.mWakeupAndSleepSensor = r0
            if (r0 == 0) goto L_0x0058
            goto L_0x0059
        L_0x0058:
            r2 = r1
        L_0x0059:
            r0 = r2
        L_0x005a:
            if (r0 == 0) goto L_0x0066
            com.android.systemui.UiOffloadThread r0 = r5.mUiOffloadThread
            com.android.keyguard.injector.KeyguardSensorInjector$registerPickupSensor$2 r1 = new com.android.keyguard.injector.KeyguardSensorInjector$registerPickupSensor$2
            r1.<init>(r5)
            r0.submit(r1)
        L_0x0066:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.injector.KeyguardSensorInjector.registerPickupSensor():void");
    }

    /* access modifiers changed from: private */
    public final void unregisterPickupSensor() {
        this.mUiOffloadThread.submit(new KeyguardSensorInjector$unregisterPickupSensor$1(this));
    }

    public void onContentChanged(@Nullable String str, @Nullable String str2) {
        if (Intrinsics.areEqual((Object) "pick_up_gesture_wakeup_mode", (Object) str)) {
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

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0004, code lost:
        r0 = r0.getDefaultSensor(r2.LARGE_AREA_TOUCH_SENSOR);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final boolean isDeviceSupportLargeAreaTouch() {
        /*
            r2 = this;
            android.hardware.SensorManager r0 = r2.mSensorManager
            if (r0 == 0) goto L_0x001a
            int r1 = r2.LARGE_AREA_TOUCH_SENSOR
            android.hardware.Sensor r0 = r0.getDefaultSensor(r1)
            if (r0 == 0) goto L_0x001a
            java.lang.String r2 = r2.LARGE_AREA_TOUCH_SENSOR_NAME
            java.lang.String r0 = r0.getName()
            boolean r2 = kotlin.jvm.internal.Intrinsics.areEqual((java.lang.Object) r2, (java.lang.Object) r0)
            if (r2 == 0) goto L_0x001a
            r2 = 1
            return r2
        L_0x001a:
            r2 = 0
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.injector.KeyguardSensorInjector.isDeviceSupportLargeAreaTouch():boolean");
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
        return ArraysKt___ArraysKt.contains(stringArray, Build.DEVICE);
    }

    public final void disableFullScreenGesture() {
        if (MiuiKeyguardUtils.isFullScreenGestureOpened()) {
            CommonUtil.updateFsgState(this.mContext, "typefrom_keyguard", this.mKeyguardViewMediator.isShowing() && !this.mKeyguardViewMediator.isOccluded() && !this.mKeyguardUpdateMonitor.isBouncerShowing());
        }
    }
}
