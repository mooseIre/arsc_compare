package com.android.keyguard.charge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.util.Slog;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.MiuiKeyguardUtils;
import com.android.keyguard.analytics.AnalyticsHelper;
import com.android.keyguard.charge.rapid.IRapidAnimationListener;
import com.android.keyguard.charge.rapid.LollipopRapidChargeView;
import com.android.keyguard.charge.rapid.RapidChargeView;
import com.android.keyguard.charge.rapid.VideoRapidChargeView;
import com.android.keyguard.charge.rapid.WaveRapidChargeView;
import com.android.keyguard.charge.rapid.WirelessRapidChargeView;
import com.android.systemui.Util;
import com.android.systemui.events.ScreenOffEvent;
import com.android.systemui.events.ScreenOnEvent;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.statusbar.KeyguardIndicationController;
import com.xiaomi.stat.c.b;
import miui.os.Build;

public class MiuiChargeController implements IRapidAnimationListener {
    private final boolean SUPPORT_NEW_ANIMATION = ChargeUtils.supportNewChargeAnimation();
    /* access modifiers changed from: private */
    public BatteryStatus mBatteryStatus;
    private int mChargeDeviceForAnalytic;
    /* access modifiers changed from: private */
    public int mChargeDeviceType;
    private int mChargeType;
    private boolean mClickShowChargeUI;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler();
    private KeyguardIndicationController mKeyguardIndicationController;
    KeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
        public void onRefreshBatteryInfo(BatteryStatus batteryStatus) {
            super.onRefreshBatteryInfo(batteryStatus);
            BatteryStatus unused = MiuiChargeController.this.mBatteryStatus = batteryStatus;
            int unused2 = MiuiChargeController.this.mChargeDeviceType = batteryStatus.chargeDeviceType;
            MiuiChargeController miuiChargeController = MiuiChargeController.this;
            miuiChargeController.checkBatteryStatus(miuiChargeController.mBatteryStatus, false);
        }

        public void onKeyguardOccludedChanged(boolean z) {
            super.onKeyguardOccludedChanged(z);
            if (z) {
                MiuiChargeController.this.dismissRapidChargeAnimation("isOccluded");
                MiuiChargeController.this.dismissWirelessRapidChargeAnimation("isOccluded");
            }
        }
    };
    private MiuiWirelessChargeSlowlyView mMiuiWirelessChargeSlowlyView;
    /* access modifiers changed from: private */
    public boolean mNeedRepositionDevice = false;
    /* access modifiers changed from: private */
    public PowerManager mPowerManager;
    /* access modifiers changed from: private */
    public boolean mRapidChargeAnimationShowing = false;
    /* access modifiers changed from: private */
    public RapidChargeView mRapidChargeView;
    /* access modifiers changed from: private */
    public final Runnable mScreenOffRunnable = new Runnable() {
        public void run() {
            if (!MiuiChargeController.this.mNeedRepositionDevice && KeyguardUpdateMonitor.getInstance(MiuiChargeController.this.mContext).isKeyguardShowing() && !KeyguardUpdateMonitor.getInstance(MiuiChargeController.this.mContext).isKeyguardOccluded()) {
                Slog.i("MiuiChargeController", "keyguard_screen_off_reason:charge animation");
                MiuiChargeController.this.mPowerManager.goToSleep(SystemClock.uptimeMillis());
            }
        }
    };
    private boolean mScreenOn = false;
    private PowerManager.WakeLock mScreenOnWakeLock;
    private final Runnable mShowSlowlyRunnable = new Runnable() {
        public void run() {
            MiuiChargeController.this.showMissedTip(true);
        }
    };
    private boolean mStateInitialized;
    private KeyguardUpdateMonitor mUpdateMonitor;
    /* access modifiers changed from: private */
    public WindowManager mWindowManager;
    private int mWirelessChargeStartLevel;
    private long mWirelessChargeStartTime;
    /* access modifiers changed from: private */
    public int mWirelessChargeState;
    /* access modifiers changed from: private */
    public View mWirelessChargeView;
    private boolean mWirelessCharging = false;
    private boolean mWirelessOnline = false;
    /* access modifiers changed from: private */
    public boolean mWirelessRapidChargeAnimationShowing = false;
    /* access modifiers changed from: private */
    public WirelessRapidChargeView mWirelessRapidChargeView;
    private boolean pendingRapidAnimation;
    private boolean pendingWirelessRapidAnimation;

    public MiuiChargeController(Context context) {
        Log.i("MiuiChargeController", "MiuiChargeController: ");
        this.mContext = context;
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mUpdateMonitor = KeyguardUpdateMonitor.getInstance(context);
        this.mBatteryStatus = new BatteryStatus(1, 0, 0, 0, 0, -1);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_PRESENT");
        intentFilter.addAction("miui.intent.action.ACTION_SOC_DECIMAL");
        intentFilter.addAction("miui.intent.action.ACTION_WIRELESS_POSITION");
        intentFilter.setPriority(b.a);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int intExtra;
                if ("miui.intent.action.ACTION_SOC_DECIMAL".equals(intent.getAction())) {
                    int intExtra2 = intent.getIntExtra("miui.intent.extra.soc_decimal", 0);
                    int intExtra3 = intent.getIntExtra("miui.intent.extra.soc_decimal_rate", 0);
                    Slog.i("MiuiChargeController", "receive soc decimal, battery:" + MiuiChargeController.this.mBatteryStatus.level + ",level:" + intExtra2 + ";rate=" + intExtra3);
                    if (MiuiChargeController.this.mBatteryStatus.level >= 100) {
                        return;
                    }
                    if (MiuiChargeController.this.mRapidChargeAnimationShowing || MiuiChargeController.this.mWirelessRapidChargeAnimationShowing) {
                        if (MiuiChargeController.this.mRapidChargeView != null) {
                            MiuiChargeController.this.mRapidChargeView.startValueAnimation(((float) MiuiChargeController.this.mBatteryStatus.level) + (((float) intExtra2) / 100.0f), ((float) intExtra3) / 100.0f);
                        }
                        if (MiuiChargeController.this.mWirelessRapidChargeView != null) {
                            MiuiChargeController.this.mWirelessRapidChargeView.startValueAnimation(((float) MiuiChargeController.this.mBatteryStatus.level) + (((float) intExtra2) / 100.0f), ((float) intExtra3) / 100.0f);
                        }
                        MiuiChargeController.this.mHandler.removeCallbacks(MiuiChargeController.this.mScreenOffRunnable);
                        MiuiChargeController.this.mHandler.postDelayed(MiuiChargeController.this.mScreenOffRunnable, 9700);
                    }
                } else if ("android.intent.action.USER_PRESENT".equals(intent.getAction())) {
                    MiuiChargeController.this.mHandler.removeCallbacks(MiuiChargeController.this.mScreenOffRunnable);
                    MiuiChargeController.this.dismissRapidChargeAnimation("USER_PRESENT");
                    MiuiChargeController.this.dismissWirelessRapidChargeAnimation("USER_PRESENT");
                } else if ("miui.intent.action.ACTION_WIRELESS_POSITION".equals(intent.getAction()) && (intExtra = intent.getIntExtra("miui.intent.extra.wireless_position", -1)) != MiuiChargeController.this.mWirelessChargeState) {
                    int unused = MiuiChargeController.this.mWirelessChargeState = intExtra;
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
        RecentsEventBus.getDefault().register(this);
        this.mUpdateMonitor.registerCallback(this.mKeyguardUpdateMonitorCallback);
        this.mWirelessChargeState = -1;
        this.mChargeDeviceType = -1;
        this.mStateInitialized = false;
        this.mChargeType = -1;
    }

    public void setKeyguardIndicationController(KeyguardIndicationController keyguardIndicationController) {
        this.mKeyguardIndicationController = keyguardIndicationController;
    }

    public final void onBusEvent(ScreenOffEvent screenOffEvent) {
        onStartedGoingToSleep();
    }

    public final void onBusEvent(ScreenOnEvent screenOnEvent) {
        onStartedWakingUp();
    }

    public void checkBatteryStatus(boolean z) {
        checkBatteryStatus(this.mBatteryStatus, z);
    }

    /* access modifiers changed from: private */
    public void checkBatteryStatus(BatteryStatus batteryStatus, boolean z) {
        boolean z2;
        boolean z3;
        boolean z4;
        if (batteryStatus != null) {
            this.mClickShowChargeUI = z;
            int i = batteryStatus.status;
            boolean z5 = false;
            boolean z6 = batteryStatus.wireState == 10;
            int checkChargeState = checkChargeState(batteryStatus);
            if (checkChargeState != this.mChargeType) {
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
            Log.i("MiuiChargeController", "checkBatteryStatus: chargeType " + checkChargeState + " status " + i + " plugged " + batteryStatus.plugged + " isRapidCharge " + z4 + " isSuperCharge " + z3 + " isCarMode " + isWirelessCarMode + " mChargeDeviceType " + this.mChargeDeviceType + " mChargeDeviceForAnalytic " + this.mChargeDeviceForAnalytic);
            if (this.mStateInitialized) {
                dealWithAnimationShow(checkChargeState, z6);
                dealWithBadlyCharge(z6, checkChargeState);
            }
            dealWithWirelessChargeAnalyticEvent(checkChargeState == 10, batteryStatus.level, this.mChargeType != checkChargeState);
            RapidChargeView rapidChargeView = this.mRapidChargeView;
            if (rapidChargeView != null && this.mRapidChargeAnimationShowing) {
                rapidChargeView.setProgress(batteryStatus.level);
                this.mRapidChargeView.setChargeState(z4, z3, z2);
            }
            WirelessRapidChargeView wirelessRapidChargeView = this.mWirelessRapidChargeView;
            if (wirelessRapidChargeView != null && this.mWirelessRapidChargeAnimationShowing) {
                wirelessRapidChargeView.setProgress(batteryStatus.level);
                this.mWirelessRapidChargeView.setChargeState(z3, isWirelessCarMode, z2);
            }
            this.mWirelessOnline = z6;
            if (checkChargeState == 10) {
                z5 = true;
            }
            this.mWirelessCharging = z5;
            this.mChargeType = checkChargeState;
            this.mStateInitialized = true;
        }
    }

    private int checkChargeState(BatteryStatus batteryStatus) {
        int i = batteryStatus.status;
        boolean z = true;
        boolean z2 = batteryStatus.wireState == 10;
        if (batteryStatus.wireState != 11) {
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

    private void dealWithAnimationShow(int i, boolean z) {
        if (shouldShowChargeAnim()) {
            if (this.mClickShowChargeUI) {
                if (this.mRapidChargeAnimationShowing || this.mWirelessRapidChargeAnimationShowing) {
                    return;
                }
            } else if (this.mChargeType == i) {
                return;
            }
            boolean isKeyguardShowing = this.mUpdateMonitor.isKeyguardShowing();
            if (i == 11 && isKeyguardShowing && !KeyguardUpdateMonitor.getInstance(this.mContext).isKeyguardOccluded()) {
                showRapidChargeAnimation();
            } else if (i != 10 || !isKeyguardShowing || KeyguardUpdateMonitor.getInstance(this.mContext).isKeyguardOccluded()) {
                this.pendingRapidAnimation = false;
                this.pendingWirelessRapidAnimation = false;
                this.mHandler.removeCallbacks(this.mScreenOffRunnable);
                dismissRapidChargeAnimation("dealWithAnimationShow");
                dismissWirelessRapidChargeAnimation("dealWithAnimationShow");
            } else {
                showWirelessRapidChargeAnimation();
            }
        }
    }

    private void dealWithBadlyCharge(boolean z, int i) {
        if (this.mWirelessOnline && !z) {
            if (i == 11) {
                showToast(R.string.wireless_change_to_ac_charging);
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
    public void setNeedRepositionDevice(boolean z) {
        this.mNeedRepositionDevice = z;
        ChargeUtils.setNeedRepositionDevice(z);
    }

    public void onRapidAnimationStart(int i) {
        Log.i("MiuiChargeController", "onRapidAnimationStart: " + i);
        this.mKeyguardIndicationController.handleChargeTextAnimation(true);
    }

    public void onRapidAnimationEnd(int i, String str) {
        MiuiKeyguardUtils.userActivity(this.mContext);
        this.mScreenOnWakeLock.release();
        this.mHandler.removeCallbacks(this.mScreenOffRunnable);
    }

    public void onRapidAnimationDismiss(int i, String str) {
        Log.i("MiuiChargeController", "onRapidAnimationDismiss: type " + i + " reason :" + str);
        if (i == 11) {
            this.mRapidChargeAnimationShowing = false;
        } else if (i == 10) {
            this.mWirelessRapidChargeAnimationShowing = false;
        }
        if (!shouldShowChargeAnim()) {
            return;
        }
        if (this.pendingRapidAnimation) {
            this.pendingRapidAnimation = false;
            showRapidChargeAnimation();
        } else if (this.pendingWirelessRapidAnimation) {
            this.pendingWirelessRapidAnimation = false;
            showWirelessRapidChargeAnimation();
        }
    }

    private void showRapidChargeAnimation() {
        Log.i("MiuiChargeController", "showRapidChargeAnimation: ");
        if (shouldShowChargeAnim()) {
            this.mHandler.removeCallbacks(this.mScreenOffRunnable);
            if (this.mWirelessRapidChargeAnimationShowing) {
                this.pendingRapidAnimation = true;
                dismissWirelessRapidChargeAnimation("showRapidChargeAnimation");
            } else if (!this.mRapidChargeAnimationShowing) {
                prepareRapidChargeView();
                this.mRapidChargeAnimationShowing = true;
                AnalyticsHelper.getInstance(this.mContext).recordChargeAnimation(11);
                this.mUpdateMonitor.setShowingChargeAnimationWindow(true);
                this.mRapidChargeView.zoomLarge(this.mScreenOn, this.mClickShowChargeUI);
                if (!this.mUpdateMonitor.isDeviceInteractive()) {
                    this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), "com.android.systemui:RAPID_CHARGE");
                }
                Log.i("MiuiChargeController", "mScreenOnWakeLock showRapidChargeAnimation: acquire");
                this.mScreenOnWakeLock.acquire((long) this.mRapidChargeView.getAnimationDuration());
                Log.i("MiuiChargeController", "showRapidChargeAnimation: mScreenOn " + this.mScreenOn);
                if (!this.mNeedRepositionDevice && this.mRapidChargeView.getAnimationDuration() > 10000) {
                    this.mHandler.removeCallbacks(this.mScreenOffRunnable);
                    this.mHandler.postDelayed(this.mScreenOffRunnable, (long) (this.mRapidChargeView.getAnimationDuration() - 300));
                }
            }
        }
    }

    private void prepareRapidChargeView() {
        if (shouldShowChargeAnim()) {
            if (this.mRapidChargeView == null) {
                if (ChargeUtils.supportWaveChargeAnimation()) {
                    this.mRapidChargeView = new WaveRapidChargeView(this.mContext);
                } else if (ChargeUtils.supportVideoChargeAnimation()) {
                    this.mRapidChargeView = new VideoRapidChargeView(this.mContext);
                } else {
                    this.mRapidChargeView = new LollipopRapidChargeView(this.mContext);
                }
                this.mRapidChargeView.setScreenOn(this.mScreenOn);
                this.mRapidChargeView.setRapidAnimationListener(this);
                this.mRapidChargeView.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (!MiuiChargeController.this.mRapidChargeAnimationShowing) {
                            return false;
                        }
                        if (!MiuiChargeController.this.mRapidChargeView.shouldDismiss()) {
                            return true;
                        }
                        MiuiChargeController.this.dismissRapidChargeAnimation("onTouch");
                        MiuiChargeController.this.mHandler.removeCallbacks(MiuiChargeController.this.mScreenOffRunnable);
                        return true;
                    }
                });
            }
            this.mRapidChargeView.setScreenOn(this.mScreenOn);
            this.mRapidChargeView.setProgress(this.mBatteryStatus.level);
            this.mRapidChargeView.setChargeState(ChargeUtils.isRapidCharge(this.mChargeDeviceType), ChargeUtils.isSuperRapidCharge(this.mChargeDeviceType), ChargeUtils.isStrongSuperRapidCharge(this.mChargeDeviceType));
            this.mRapidChargeView.addToWindow("prepareRapidChargeView");
        }
    }

    public void dismissRapidChargeAnimation(String str) {
        Log.i("MiuiChargeController", "dismissRapidChargeAnimation: " + str);
        this.mUpdateMonitor.setShowingChargeAnimationWindow(false);
        if (shouldShowChargeAnim() && this.mRapidChargeAnimationShowing) {
            this.mKeyguardIndicationController.handleChargeTextAnimation(false);
            RapidChargeView rapidChargeView = this.mRapidChargeView;
            if (rapidChargeView != null) {
                rapidChargeView.startDismiss(str);
            }
            this.mRapidChargeAnimationShowing = false;
        }
    }

    private void showWirelessRapidChargeAnimation() {
        Log.i("MiuiChargeController", "showWirelessRapidChargeAnimation: ");
        if (shouldShowChargeAnim()) {
            this.mHandler.removeCallbacks(this.mScreenOffRunnable);
            if (this.mRapidChargeAnimationShowing) {
                this.pendingWirelessRapidAnimation = true;
                dismissRapidChargeAnimation("showWirelessRapidChargeAnimation");
            } else if (!this.mWirelessRapidChargeAnimationShowing) {
                prepareWirelessRapidChargeView();
                this.mWirelessRapidChargeAnimationShowing = true;
                AnalyticsHelper.getInstance(this.mContext).recordChargeAnimation(10);
                KeyguardUpdateMonitor instance = KeyguardUpdateMonitor.getInstance(this.mContext);
                this.mUpdateMonitor.setShowingChargeAnimationWindow(true);
                this.mWirelessRapidChargeView.zoomLarge(this.mScreenOn, this.mClickShowChargeUI);
                if (!instance.isDeviceInteractive()) {
                    this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), "com.android.systemui:WIRELESS_RAPID_CHARGE");
                }
                Log.i("MiuiChargeController", "mScreenOnWakeLock showWirelessRapidChargeAnimation: acquire");
                this.mScreenOnWakeLock.acquire(20000);
                Log.i("MiuiChargeController", "showWirelessRapidChargeAnimation: mScreenOn " + this.mScreenOn);
                if (!this.mNeedRepositionDevice) {
                    this.mHandler.removeCallbacks(this.mScreenOffRunnable);
                    this.mHandler.postDelayed(this.mScreenOffRunnable, 19700);
                }
            }
        }
    }

    private void prepareWirelessRapidChargeView() {
        if (shouldShowChargeAnim()) {
            if (this.mWirelessRapidChargeView == null) {
                WirelessRapidChargeView wirelessRapidChargeView = new WirelessRapidChargeView(this.mContext);
                this.mWirelessRapidChargeView = wirelessRapidChargeView;
                wirelessRapidChargeView.setScreenOn(this.mScreenOn);
                this.mWirelessRapidChargeView.setRapidAnimationListener(this);
                this.mWirelessRapidChargeView.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (!MiuiChargeController.this.mWirelessRapidChargeAnimationShowing) {
                            return false;
                        }
                        MiuiChargeController.this.dismissWirelessRapidChargeAnimation("onTouch");
                        MiuiChargeController.this.mHandler.removeCallbacks(MiuiChargeController.this.mScreenOffRunnable);
                        return true;
                    }
                });
            }
            this.mWirelessRapidChargeView.setScreenOn(this.mScreenOn);
            this.mWirelessRapidChargeView.setProgress(this.mBatteryStatus.level);
            this.mWirelessRapidChargeView.setChargeState(ChargeUtils.isWirelessSuperRapidCharge(this.mChargeDeviceType), ChargeUtils.isWirelessCarMode(this.mChargeDeviceType), ChargeUtils.isStrongSuperRapidCharge(this.mChargeDeviceType));
            this.mWirelessRapidChargeView.addToWindow("prepareWirelessRapidChargeView");
        }
    }

    public void dismissWirelessRapidChargeAnimation(String str) {
        Log.i("MiuiChargeController", "dismissWirelessRapidChargeAnimation: " + str);
        this.mUpdateMonitor.setShowingChargeAnimationWindow(false);
        if (shouldShowChargeAnim() && this.mWirelessRapidChargeAnimationShowing) {
            this.mKeyguardIndicationController.handleChargeTextAnimation(false);
            WirelessRapidChargeView wirelessRapidChargeView = this.mWirelessRapidChargeView;
            if (wirelessRapidChargeView != null) {
                wirelessRapidChargeView.startDismiss(str);
            }
            this.mWirelessRapidChargeAnimationShowing = false;
        }
    }

    private void showToast(int i) {
        Util.showSystemOverlayToast(this.mContext, i, 1);
    }

    private void checkWirelessChargeEfficiency() {
        new AsyncTask<Void, Void, Integer>() {
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
                throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.charge.MiuiChargeController.AnonymousClass6.doInBackground(java.lang.Void[]):java.lang.Integer");
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
    public void checkIfShowWirelessChargeSlowly() {
        new AsyncTask<Void, Void, Boolean>() {
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

    /* access modifiers changed from: private */
    public void showWirelessChargeSlowly() {
        this.mHandler.postDelayed(this.mShowSlowlyRunnable, 2000);
    }

    private void onStartedGoingToSleep() {
        this.mScreenOn = false;
        if (shouldShowChargeAnim()) {
            showMissedTip(false);
            if (ChargeUtils.supportWaveChargeAnimation()) {
                prepareRapidChargeView();
                prepareWirelessRapidChargeView();
            }
            dismissRapidChargeAnimation("screen off");
            dismissWirelessRapidChargeAnimation("screen off");
        }
        this.mHandler.removeCallbacks(this.mScreenOffRunnable);
    }

    private void onStartedWakingUp() {
        this.mScreenOn = true;
        RapidChargeView rapidChargeView = this.mRapidChargeView;
        if (rapidChargeView != null) {
            rapidChargeView.setScreenOn(true);
        }
        WirelessRapidChargeView wirelessRapidChargeView = this.mWirelessRapidChargeView;
        if (wirelessRapidChargeView != null) {
            wirelessRapidChargeView.setScreenOn(true);
        }
        this.mHandler.post(new Runnable() {
            public void run() {
                if (MiuiChargeController.this.mWirelessChargeView != null) {
                    MiuiChargeController.this.mWindowManager.removeView(MiuiChargeController.this.mWirelessChargeView);
                    View unused = MiuiChargeController.this.mWirelessChargeView = null;
                }
                if (MiuiChargeController.this.mRapidChargeView != null && !MiuiChargeController.this.mRapidChargeAnimationShowing) {
                    MiuiChargeController.this.mRapidChargeView.removeFromWindow("onStartedWakingUp");
                }
                if (MiuiChargeController.this.mWirelessRapidChargeView != null && !MiuiChargeController.this.mWirelessRapidChargeAnimationShowing) {
                    MiuiChargeController.this.mWirelessRapidChargeView.removeFromWindow("onStartedWakingUp");
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public void showMissedTip(boolean z) {
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
}
