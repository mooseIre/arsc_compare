package com.android.keyguard.charge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.util.Slog;
import android.view.MotionEvent;
import android.view.View;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;
import com.android.keyguard.analytics.AnalyticsHelper;
import com.android.keyguard.charge.container.MiuiChargeAnimationView;
import com.android.keyguard.charge.view.IChargeAnimationListener;
import com.android.keyguard.injector.KeyguardUpdateMonitorInjector;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.statusbar.KeyguardIndicationController;
import com.android.systemui.statusbar.phone.StatusBar;
import com.miui.systemui.SettingsObserver;
import com.miui.systemui.util.HapticFeedBackImpl;
import com.miui.systemui.util.MiuiTextUtils;
import miui.os.Build;
import org.jetbrains.annotations.Nullable;

public class MiuiChargeController implements IChargeAnimationListener, WakefulnessLifecycle.Observer, SettingsObserver.Callback {
    private final boolean SUPPORT_NEW_ANIMATION = ChargeUtils.supportNewChargeAnimation();
    private Sensor mAngleSensor;
    private MiuiBatteryStatus mBatteryStatus;
    private boolean mChargeAnimationShowing = false;
    private MiuiChargeAnimationView mChargeAnimationView;
    private int mChargeDeviceForAnalytic;
    private int mChargeDeviceType;
    private int mChargeSpeed = -1;
    private boolean mClickShowChargeUI;
    private Context mContext;
    private Boolean mFoldStatus;
    private Handler mHandler = new Handler();
    private boolean mIsFoldChargeVideo;
    private KeyguardIndicationController mKeyguardIndicationController;
    MiuiKeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback = new MiuiKeyguardUpdateMonitorCallback() {
        /* class com.android.keyguard.charge.MiuiChargeController.AnonymousClass1 */

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onRefreshBatteryInfo(MiuiBatteryStatus miuiBatteryStatus) {
            super.onRefreshBatteryInfo(miuiBatteryStatus);
            MiuiChargeController.this.mBatteryStatus = miuiBatteryStatus;
            MiuiChargeController.this.mChargeDeviceType = miuiBatteryStatus.chargeDeviceType;
            MiuiChargeController miuiChargeController = MiuiChargeController.this;
            miuiChargeController.checkBatteryStatus(miuiChargeController.mBatteryStatus, false);
        }

        @Override // com.android.keyguard.MiuiKeyguardUpdateMonitorCallback
        public void onKeyguardOccludedChanged(boolean z) {
            super.onKeyguardOccludedChanged(z);
            if (z) {
                MiuiChargeController.this.dismissChargeAnimation("isOccluded");
            }
        }
    };
    private MiuiWirelessChargeSlowlyView mMiuiWirelessChargeSlowlyView;
    private boolean mNeedRepositionDevice = false;
    private boolean mPendingChargeAnimation;
    private PowerManager mPowerManager;
    private final Runnable mScreenOffRunnable = new Runnable() {
        /* class com.android.keyguard.charge.MiuiChargeController.AnonymousClass4 */

        public void run() {
            if (!MiuiChargeController.this.mNeedRepositionDevice && MiuiChargeController.this.mUpdateMonitorInjector.isKeyguardShowing() && !MiuiChargeController.this.mUpdateMonitorInjector.isKeyguardOccluded()) {
                Slog.i("MiuiChargeController", "keyguard_screen_off_reason: charge animation");
                MiuiChargeController.this.mPowerManager.goToSleep(SystemClock.uptimeMillis());
            }
        }
    };
    private boolean mScreenOn = false;
    private PowerManager.WakeLock mScreenOnWakeLock;
    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        /* class com.android.keyguard.charge.MiuiChargeController.AnonymousClass8 */

        public void onAccuracyChanged(Sensor sensor, int i) {
        }

        public void onSensorChanged(SensorEvent sensorEvent) {
            boolean z = false;
            if (sensorEvent.values[0] != 0.0f) {
                z = true;
            }
            if (MiuiChargeController.this.mFoldStatus == null || MiuiChargeController.this.mFoldStatus.booleanValue() != z) {
                if (MiuiChargeController.this.mFoldStatus != null) {
                    MiuiChargeController.this.dismissChargeAnimation("fold_state_changed");
                }
                MiuiChargeController.this.mFoldStatus = Boolean.valueOf(z);
            }
        }
    };
    private SensorManager mSensorManager;
    private boolean mShowChargingFromSetting;
    private boolean mShowChargingInNonLockscreen;
    private final Runnable mShowSlowlyRunnable = new Runnable() {
        /* class com.android.keyguard.charge.MiuiChargeController.AnonymousClass7 */

        public void run() {
            MiuiChargeController.this.showMissedTip(true);
        }
    };
    private boolean mStateInitialized;
    private KeyguardUpdateMonitor mUpdateMonitor;
    private KeyguardUpdateMonitorInjector mUpdateMonitorInjector;
    private int mWireState;
    private int mWirelessChargeStartLevel;
    private long mWirelessChargeStartTime;
    private int mWirelessChargeState;
    private boolean mWirelessCharging = false;
    private boolean mWirelessOnline = false;

    public MiuiChargeController(Context context, WakefulnessLifecycle wakefulnessLifecycle) {
        Log.i("MiuiChargeController", "MiuiChargeController: init");
        this.mContext = context;
        this.mUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        this.mUpdateMonitorInjector = (KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class);
        this.mUpdateMonitor.registerCallback(this.mKeyguardUpdateMonitorCallback);
        wakefulnessLifecycle.addObserver(this);
        this.mKeyguardIndicationController = (KeyguardIndicationController) Dependency.get(KeyguardIndicationController.class);
        ((SettingsObserver) Dependency.get(SettingsObserver.class)).addCallback(this, "show_charging_in_non_lockscreen");
        this.mBatteryStatus = new MiuiBatteryStatus(1, 0, 0, 0, 0, -1, 1, -1);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_PRESENT");
        intentFilter.addAction("miui.intent.action.ACTION_SOC_DECIMAL");
        intentFilter.addAction("miui.intent.action.ACTION_WIRELESS_POSITION");
        intentFilter.setPriority(1001);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.keyguard.charge.MiuiChargeController.AnonymousClass2 */

            public void onReceive(Context context, Intent intent) {
                int intExtra;
                if ("miui.intent.action.ACTION_SOC_DECIMAL".equals(intent.getAction())) {
                    int intExtra2 = intent.getIntExtra("miui.intent.extra.soc_decimal", 0);
                    int intExtra3 = intent.getIntExtra("miui.intent.extra.soc_decimal_rate", 0);
                    Slog.i("MiuiChargeController", "receive soc decimal, battery:" + MiuiChargeController.this.mBatteryStatus.level + ",level:" + intExtra2 + ";rate=" + intExtra3);
                    if (MiuiChargeController.this.mBatteryStatus.level < 100 && MiuiChargeController.this.mChargeAnimationShowing) {
                        if (MiuiChargeController.this.mChargeAnimationView != null) {
                            MiuiChargeController.this.mChargeAnimationView.startValueAnimation(((float) MiuiChargeController.this.mBatteryStatus.level) + (((float) intExtra2) / 100.0f), ((float) intExtra3) / 100.0f);
                        }
                        MiuiChargeController.this.mHandler.removeCallbacks(MiuiChargeController.this.mScreenOffRunnable);
                        MiuiChargeController.this.mHandler.postDelayed(MiuiChargeController.this.mScreenOffRunnable, 9700);
                    }
                } else if ("android.intent.action.USER_PRESENT".equals(intent.getAction())) {
                    MiuiChargeController.this.mHandler.removeCallbacks(MiuiChargeController.this.mScreenOffRunnable);
                    MiuiChargeController.this.dismissChargeAnimation("USER_PRESENT");
                } else if ("miui.intent.action.ACTION_WIRELESS_POSITION".equals(intent.getAction()) && (intExtra = intent.getIntExtra("miui.intent.extra.wireless_position", -1)) != MiuiChargeController.this.mWirelessChargeState) {
                    MiuiChargeController.this.mWirelessChargeState = intExtra;
                    if (intExtra == 0) {
                        MiuiChargeController.this.setNeedRepositionDevice(true);
                        MiuiChargeController.this.showMissedTip(true);
                    } else if (intExtra == 1) {
                        MiuiChargeController.this.setNeedRepositionDevice(false);
                        MiuiChargeController.this.showMissedTip(false);
                    }
                }
            }
        }, intentFilter);
        PowerManager powerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mPowerManager = powerManager;
        PowerManager.WakeLock newWakeLock = powerManager.newWakeLock(10, "wireless_charge");
        this.mScreenOnWakeLock = newWakeLock;
        newWakeLock.setReferenceCounted(false);
        this.mWirelessChargeState = -1;
        this.mChargeDeviceType = -1;
        this.mStateInitialized = false;
        this.mWireState = -1;
        this.mIsFoldChargeVideo = context.getResources().getBoolean(C0010R$bool.config_folding_charge_video);
        SensorManager sensorManager = (SensorManager) this.mContext.getSystemService(SensorManager.class);
        this.mSensorManager = sensorManager;
        this.mAngleSensor = sensorManager.getDefaultSensor(33171087);
    }

    public void checkBatteryStatus(boolean z) {
        checkBatteryStatus(this.mBatteryStatus, z);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkBatteryStatus(MiuiBatteryStatus miuiBatteryStatus, boolean z) {
        boolean z2;
        boolean z3;
        boolean z4;
        MiuiBatteryStatus miuiBatteryStatus2;
        if (miuiBatteryStatus != null) {
            this.mBatteryStatus = miuiBatteryStatus;
            this.mChargeDeviceType = miuiBatteryStatus.chargeDeviceType;
            this.mClickShowChargeUI = z;
            int i = miuiBatteryStatus.status;
            boolean z5 = false;
            boolean z6 = miuiBatteryStatus.wireState == 10;
            int checkChargeState = checkChargeState(miuiBatteryStatus);
            int i2 = this.mWireState;
            if (!(i2 == -1 || checkChargeState == i2)) {
                this.mChargeDeviceType = -1;
            }
            boolean isWirelessCarMode = ChargeUtils.isWirelessCarMode(this.mChargeDeviceType);
            if (checkChargeState == 10) {
                boolean isWirelessSuperRapidCharge = ChargeUtils.isWirelessSuperRapidCharge(this.mChargeDeviceType);
                z2 = ChargeUtils.isWirelessStrongSuperRapidCharge(this.mChargeDeviceType);
                z3 = isWirelessSuperRapidCharge;
                z4 = false;
            } else if (checkChargeState == 11) {
                z4 = ChargeUtils.isRapidCharge(this.mChargeDeviceType);
                z3 = ChargeUtils.isSuperRapidCharge(this.mChargeDeviceType);
                z2 = ChargeUtils.isStrongSuperRapidCharge(this.mChargeDeviceType);
            } else {
                z4 = false;
                z3 = false;
                z2 = false;
            }
            Log.i("MiuiChargeController", "checkBatteryStatus: wireState " + checkChargeState + " status " + i + " plugged " + miuiBatteryStatus.plugged + " chargeSpeed " + miuiBatteryStatus.chargeSpeed + " maxChargingWattage " + miuiBatteryStatus.maxChargingWattage + " isRapidCharge " + z4 + " isSuperCharge " + z3 + " isStrongSuperCharge " + z2 + " isCarMode " + isWirelessCarMode + " mChargeDeviceType " + this.mChargeDeviceType + " mChargeDeviceForAnalytic " + this.mChargeDeviceForAnalytic + " SUPPORT_NEW_ANIMATION " + this.SUPPORT_NEW_ANIMATION + " isChargeAnimationDisabled " + ChargeUtils.isChargeAnimationDisabled());
            if (this.mKeyguardIndicationController != null && ((miuiBatteryStatus2 = ChargeUtils.sBatteryStatus) == null || this.mBatteryStatus.level != miuiBatteryStatus2.level)) {
                this.mKeyguardIndicationController.updatePowerIndication(this.mChargeAnimationShowing);
            }
            ChargeUtils.setBatteryStatus(this.mBatteryStatus);
            if (this.mStateInitialized) {
                dealWithAnimationShow(checkChargeState);
                dealWithBadlyCharge(z6, checkChargeState);
            }
            dealWithWirelessChargeAnalyticEvent(checkChargeState == 10, miuiBatteryStatus.level, this.mWireState != checkChargeState);
            MiuiChargeAnimationView miuiChargeAnimationView = this.mChargeAnimationView;
            if (miuiChargeAnimationView != null && this.mChargeAnimationShowing) {
                miuiChargeAnimationView.setProgress(miuiBatteryStatus.level);
                switchChargeItemViewAnimation(miuiBatteryStatus, z);
            }
            this.mWirelessOnline = z6;
            if (checkChargeState == 10) {
                z5 = true;
            }
            this.mWirelessCharging = z5;
            this.mWireState = checkChargeState;
            this.mStateInitialized = true;
            if (!miuiBatteryStatus.isPluggedIn()) {
                this.mChargeSpeed = -1;
            }
        }
    }

    private void switchChargeItemViewAnimation(MiuiBatteryStatus miuiBatteryStatus, boolean z) {
        int chargeSpeed = ChargeUtils.getChargeSpeed(miuiBatteryStatus.wireState, miuiBatteryStatus.chargeDeviceType);
        if (this.mChargeSpeed != chargeSpeed && this.mChargeAnimationView != null) {
            Log.d("MiuiChargeController", "switchChargeItemViewAnimation: " + chargeSpeed + ",chargeDeviceType=" + miuiBatteryStatus.chargeDeviceType);
            this.mChargeSpeed = chargeSpeed;
            this.mChargeAnimationView.switchChargeItemViewAnimation(z, chargeSpeed);
        }
    }

    private int checkChargeState(MiuiBatteryStatus miuiBatteryStatus) {
        int i = miuiBatteryStatus.status;
        boolean z = true;
        boolean z2 = miuiBatteryStatus.wireState == 10;
        if (miuiBatteryStatus.wireState != 11) {
            z = false;
        }
        if (i != 2 && i != 5 && i != 4) {
            return -1;
        }
        if (z2) {
            return 10;
        }
        return z ? 11 : -1;
    }

    private void dealWithAnimationShow(int i) {
        if (shouldShowChargeAnim()) {
            Log.i("MiuiChargeController", "dealWithAnimationShow mWireState=" + this.mWireState + ",wireState=" + i);
            if (this.mClickShowChargeUI) {
                if (this.mChargeAnimationShowing) {
                    return;
                }
            } else if (this.mWireState == i) {
                Log.i("MiuiChargeController", " dealWithAnimationShow 相同 ");
                return;
            }
            boolean isKeyguardShowing = this.mUpdateMonitorInjector.isKeyguardShowing();
            boolean isKeyguardOccluded = this.mUpdateMonitorInjector.isKeyguardOccluded();
            if (i == -1) {
                this.mPendingChargeAnimation = false;
                this.mHandler.removeCallbacks(this.mScreenOffRunnable);
                dismissChargeAnimation("dealWithAnimationShow");
            } else if (!isKeyguardShowing) {
                boolean isShowChargingInNonLockscreen = isShowChargingInNonLockscreen();
                this.mShowChargingInNonLockscreen = isShowChargingInNonLockscreen;
                if (isShowChargingInNonLockscreen) {
                    showChargeAnimation(i);
                }
                ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).getHapticFeedbackUtil().performHapticFeedback(0, false);
            } else if (!isKeyguardOccluded) {
                showChargeAnimation(i);
                ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).extHapticFeedback(74, true, 0);
            }
        }
    }

    private boolean isShowChargingInNonLockscreen() {
        boolean z = !((StatusBar) Dependency.get(StatusBar.class)).inFullscreenMode();
        Log.d("MiuiChargeController", "isAddToWindow：notFullScreen=" + z + ",mShowChargingFromSetting=" + this.mShowChargingFromSetting);
        if (!z || !this.mShowChargingFromSetting) {
            return false;
        }
        return true;
    }

    @Override // com.miui.systemui.SettingsObserver.Callback
    public void onContentChanged(@Nullable String str, @Nullable String str2) {
        if ("show_charging_in_non_lockscreen".equals(str)) {
            this.mShowChargingFromSetting = MiuiTextUtils.parseBoolean(str2, true);
            Log.d("MiuiChargeController", "onContentChanged：mShowChargingFromSetting: " + this.mShowChargingFromSetting);
        }
    }

    private void showChargeAnimation(int i) {
        Log.i("MiuiChargeController", " showChargeAnimation: wireState=" + i);
        if (shouldShowChargeAnim() && !this.mPendingChargeAnimation) {
            this.mHandler.removeCallbacks(this.mScreenOffRunnable);
            if (!this.mChargeAnimationShowing) {
                prepareChargeAnimation(i);
                AnalyticsHelper.getInstance(this.mContext).recordChargeAnimation(this.mWireState);
                this.mChargeAnimationShowing = true;
                if (this.mIsFoldChargeVideo) {
                    registerAngleSensorListener();
                }
                this.mChargeAnimationView.startChargeAnimation(this.mScreenOn, this.mClickShowChargeUI);
                if (!this.mUpdateMonitor.isDeviceInteractive()) {
                    this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), "com.android.systemui:RAPID_CHARGE");
                }
                Log.i("MiuiChargeController", "mScreenOnWakeLock showChargeAnimation: acquire");
                this.mScreenOnWakeLock.acquire((long) this.mChargeAnimationView.getAnimationDuration());
                Log.i("MiuiChargeController", "showChargeAnimation: mScreenOn " + this.mScreenOn);
                if (!this.mNeedRepositionDevice && this.mChargeAnimationView.getAnimationDuration() > 10000) {
                    this.mHandler.removeCallbacks(this.mScreenOffRunnable);
                    this.mHandler.postDelayed(this.mScreenOffRunnable, (long) (this.mChargeAnimationView.getAnimationDuration() - 300));
                }
            } else if (this.mWireState != i) {
                this.mPendingChargeAnimation = true;
                dismissChargeAnimation("changeChargeAnimation");
            }
        }
    }

    public void dismissChargeAnimation(String str) {
        Log.i("MiuiChargeController", "dismissChargeAnimation: " + str);
        if (shouldShowChargeAnim() && this.mChargeAnimationShowing) {
            if (this.mBatteryStatus.isPluggedIn()) {
                this.mKeyguardIndicationController.updatePowerIndication(false);
            }
            MiuiChargeAnimationView miuiChargeAnimationView = this.mChargeAnimationView;
            if (miuiChargeAnimationView != null) {
                miuiChargeAnimationView.startDismiss(str);
            }
            this.mChargeAnimationShowing = false;
        }
    }

    private void prepareChargeAnimation(int i) {
        if (shouldShowChargeAnim()) {
            if (this.mChargeAnimationView == null) {
                Log.d("MiuiChargeController", "prepareChargeAnimation: init mChargeAnimationView ");
                MiuiChargeAnimationView miuiChargeAnimationView = new MiuiChargeAnimationView(this.mContext);
                this.mChargeAnimationView = miuiChargeAnimationView;
                miuiChargeAnimationView.setChargeAnimationListener(this);
                this.mChargeAnimationView.setOnTouchListener(new View.OnTouchListener() {
                    /* class com.android.keyguard.charge.MiuiChargeController.AnonymousClass3 */

                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (!MiuiChargeController.this.mChargeAnimationShowing) {
                            return false;
                        }
                        MiuiChargeController.this.dismissChargeAnimation("onTouch");
                        MiuiChargeController.this.mHandler.removeCallbacks(MiuiChargeController.this.mScreenOffRunnable);
                        return true;
                    }
                });
            }
            this.mChargeAnimationView.setProgress(this.mBatteryStatus.level);
            switchChargeItemViewAnimation(this.mBatteryStatus, this.mClickShowChargeUI);
            this.mChargeAnimationView.addChargeView("prepareChargeAnimation", this.mShowChargingInNonLockscreen);
            this.mChargeAnimationView.setFocusable(true);
            this.mChargeAnimationView.setFocusableInTouchMode(true);
            this.mChargeAnimationView.requestFocus();
        }
    }

    private void dealWithBadlyCharge(boolean z, int i) {
        if (this.mWirelessOnline && !z) {
            if (i == 11) {
                showToast(C0021R$string.wireless_change_to_ac_charging);
            }
            setNeedRepositionDevice(false);
            this.mHandler.removeCallbacks(this.mScreenOffRunnable);
        }
        if (i == 11) {
            setNeedRepositionDevice(false);
            showMissedTip(false);
        }
        if (!this.mWirelessCharging && i == 10 && "polaris".equals(Build.DEVICE)) {
            checkWirelessChargeEfficiency();
        }
    }

    private void dealWithWirelessChargeAnalyticEvent(boolean z, int i, boolean z2) {
        if (z2 && z) {
            this.mWirelessChargeStartTime = System.currentTimeMillis();
            this.mWirelessChargeStartLevel = i;
        } else if (z2 && !z) {
            if (this.mWirelessChargeStartTime > 0) {
                int i2 = i - this.mWirelessChargeStartLevel;
                AnalyticsHelper.getInstance(this.mContext).recordWirelessChargeEfficiency(System.currentTimeMillis() - this.mWirelessChargeStartTime, i2, this.mChargeDeviceForAnalytic);
            }
            this.mWirelessChargeStartTime = -1;
            this.mWirelessChargeStartLevel = -1;
            this.mChargeDeviceForAnalytic = -1;
        } else if (!z2 && z && this.mWirelessChargeStartTime > 0 && i >= 100) {
            int i3 = i - this.mWirelessChargeStartLevel;
            AnalyticsHelper.getInstance(this.mContext).recordWirelessChargeEfficiency(System.currentTimeMillis() - this.mWirelessChargeStartTime, i3, this.mChargeDeviceForAnalytic);
            this.mWirelessChargeStartTime = -1;
            this.mWirelessChargeStartLevel = -1;
            this.mChargeDeviceForAnalytic = -1;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setNeedRepositionDevice(boolean z) {
        this.mNeedRepositionDevice = z;
        ChargeUtils.setNeedRepositionDevice(z);
    }

    @Override // com.android.keyguard.charge.view.IChargeAnimationListener
    public void onChargeAnimationStart(int i) {
        Log.i("MiuiChargeController", "onChargeAnimationStart: " + i);
        this.mKeyguardIndicationController.updatePowerIndication(true);
    }

    @Override // com.android.keyguard.charge.view.IChargeAnimationListener
    public void onChargeAnimationEnd(int i, String str) {
        this.mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
        this.mScreenOnWakeLock.release();
        this.mHandler.removeCallbacks(this.mScreenOffRunnable);
    }

    @Override // com.android.keyguard.charge.view.IChargeAnimationListener
    public void onChargeAnimationDismiss(int i, String str) {
        Log.i("MiuiChargeController", " onChargeAnimationDismiss: wireState " + i + " reason :" + str);
        this.mChargeAnimationShowing = false;
        if (this.mIsFoldChargeVideo) {
            unregisterAngleSensorListener();
        }
        this.mShowChargingInNonLockscreen = false;
        if (shouldShowChargeAnim() && this.mPendingChargeAnimation) {
            Log.d("MiuiChargeController", " onChargeAnimationDismiss: 切换动画 mWireState=" + this.mWireState);
            this.mPendingChargeAnimation = false;
            int i2 = this.mWireState;
            if (i2 != i) {
                showChargeAnimation(i2);
            }
        }
    }

    private void checkWirelessChargeEfficiency() {
        new AsyncTask<Void, Void, Integer>() {
            /* class com.android.keyguard.charge.MiuiChargeController.AnonymousClass5 */

            /* access modifiers changed from: protected */
            /* JADX WARNING: Removed duplicated region for block: B:17:0x0025 A[SYNTHETIC, Splitter:B:17:0x0025] */
            /* JADX WARNING: Removed duplicated region for block: B:26:0x0036 A[SYNTHETIC, Splitter:B:26:0x0036] */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public java.lang.Integer doInBackground(java.lang.Void... r3) {
                /*
                    r2 = this;
                    r2 = 0
                    java.io.FileReader r3 = new java.io.FileReader     // Catch:{ Exception -> 0x001c, all -> 0x0017 }
                    java.lang.String r0 = "/sys/class/power_supply/wireless/signal_strength"
                    r3.<init>(r0)     // Catch:{ Exception -> 0x001c, all -> 0x0017 }
                    int r2 = r3.read()     // Catch:{ Exception -> 0x0015 }
                    r3.close()     // Catch:{ IOException -> 0x0010 }
                    goto L_0x002e
                L_0x0010:
                    r3 = move-exception
                    r3.printStackTrace()
                    goto L_0x002e
                L_0x0015:
                    r2 = move-exception
                    goto L_0x0020
                L_0x0017:
                    r3 = move-exception
                    r1 = r3
                    r3 = r2
                    r2 = r1
                    goto L_0x0034
                L_0x001c:
                    r3 = move-exception
                    r1 = r3
                    r3 = r2
                    r2 = r1
                L_0x0020:
                    r2.printStackTrace()     // Catch:{ all -> 0x0033 }
                    if (r3 == 0) goto L_0x002d
                    r3.close()     // Catch:{ IOException -> 0x0029 }
                    goto L_0x002d
                L_0x0029:
                    r2 = move-exception
                    r2.printStackTrace()
                L_0x002d:
                    r2 = -1
                L_0x002e:
                    java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
                    return r2
                L_0x0033:
                    r2 = move-exception
                L_0x0034:
                    if (r3 == 0) goto L_0x003e
                    r3.close()     // Catch:{ IOException -> 0x003a }
                    goto L_0x003e
                L_0x003a:
                    r3 = move-exception
                    r3.printStackTrace()
                L_0x003e:
                    throw r2
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.charge.MiuiChargeController.AnonymousClass5.doInBackground(java.lang.Void[]):java.lang.Integer");
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Integer num) {
                Log.i("MiuiChargeController", "checkWirelessChargeEfficiency: value = " + num);
                if (num.intValue() == 48) {
                    MiuiChargeController.this.checkIfShowWirelessChargeSlowly();
                    MiuiChargeController.this.setNeedRepositionDevice(true);
                } else if (num.intValue() == 49) {
                    MiuiChargeController.this.showMissedTip(false);
                } else if (num.intValue() != 50) {
                    Log.e("MiuiChargeController", "impossible value=" + num);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkIfShowWirelessChargeSlowly() {
        new AsyncTask<Void, Void, Boolean>() {
            /* class com.android.keyguard.charge.MiuiChargeController.AnonymousClass6 */

            /* access modifiers changed from: protected */
            public Boolean doInBackground(Void... voidArr) {
                boolean z = false;
                SharedPreferences sharedPreferences = MiuiChargeController.this.mContext.getSharedPreferences("wireless_charge", 0);
                if ("polaris".equals(Build.DEVICE) && sharedPreferences.getBoolean("show_dialog", true)) {
                    z = true;
                }
                return Boolean.valueOf(z);
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Boolean bool) {
                if (bool.booleanValue()) {
                    MiuiChargeController.this.showWirelessChargeSlowly();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    private void showToast(int i) {
        ChargeUtils.showSystemOverlayToast(this.mContext, i, 1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showWirelessChargeSlowly() {
        this.mHandler.postDelayed(this.mShowSlowlyRunnable, 2000);
    }

    @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
    public void onStartedGoingToSleep() {
        this.mScreenOn = false;
        if (shouldShowChargeAnim()) {
            showMissedTip(false);
            if (ChargeUtils.supportWaveChargeAnimation()) {
                prepareChargeAnimation(this.mWireState);
            }
            dismissChargeAnimation("dismiss_for_screen_off");
        }
        this.mHandler.removeCallbacks(this.mScreenOffRunnable);
    }

    @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
    public void onStartedWakingUp() {
        this.mScreenOn = true;
        this.mHandler.post(new Runnable() {
            /* class com.android.keyguard.charge.$$Lambda$MiuiChargeController$YIliaQZAut_8QrEEj6IFrgg_Pys */

            public final void run() {
                MiuiChargeController.this.lambda$onStartedWakingUp$0$MiuiChargeController();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onStartedWakingUp$0 */
    public /* synthetic */ void lambda$onStartedWakingUp$0$MiuiChargeController() {
        MiuiChargeAnimationView miuiChargeAnimationView = this.mChargeAnimationView;
        if (miuiChargeAnimationView != null && !this.mChargeAnimationShowing) {
            miuiChargeAnimationView.removeChargeView("onStartedWakingUp");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showMissedTip(boolean z) {
        if (z) {
            if (this.mMiuiWirelessChargeSlowlyView == null) {
                this.mMiuiWirelessChargeSlowlyView = new MiuiWirelessChargeSlowlyView(this.mContext, !shouldShowChargeAnim());
            }
            AnalyticsHelper.getInstance(this.mContext).record("charge_slow");
            this.mMiuiWirelessChargeSlowlyView.show();
            return;
        }
        this.mHandler.removeCallbacks(this.mShowSlowlyRunnable);
        MiuiWirelessChargeSlowlyView miuiWirelessChargeSlowlyView = this.mMiuiWirelessChargeSlowlyView;
        if (miuiWirelessChargeSlowlyView != null) {
            miuiWirelessChargeSlowlyView.dismiss();
        }
    }

    private boolean shouldShowChargeAnim() {
        return this.SUPPORT_NEW_ANIMATION && !ChargeUtils.isChargeAnimationDisabled();
    }

    private void registerAngleSensorListener() {
        Sensor sensor = this.mAngleSensor;
        if (sensor != null) {
            this.mSensorManager.registerListener(this.mSensorEventListener, sensor, 0);
        }
    }

    private void unregisterAngleSensorListener() {
        Sensor sensor = this.mAngleSensor;
        if (sensor != null) {
            this.mFoldStatus = null;
            this.mSensorManager.unregisterListener(this.mSensorEventListener, sensor);
        }
    }
}
