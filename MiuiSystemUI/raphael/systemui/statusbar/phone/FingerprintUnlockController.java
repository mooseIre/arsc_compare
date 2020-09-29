package com.android.systemui.statusbar.phone;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Trace;
import android.provider.MiuiSettings;
import android.util.Log;
import android.util.Slog;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.LatencyTracker;
import com.android.keyguard.MiuiFastUnlockController;
import com.android.keyguard.MiuiKeyguardFingerprintUtils$FingerprintIdentificationState;
import com.android.keyguard.MiuiKeyguardUtils;
import com.android.keyguard.analytics.AnalyticsHelper;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.systemui.Dependency;
import com.android.systemui.keyguard.KeyguardViewMediator;

public class FingerprintUnlockController extends KeyguardUpdateMonitorCallback {
    private boolean mCancelingPendingLock = false;
    private final Context mContext;
    private DozeScrimController mDozeScrimController;
    private MiuiKeyguardFingerprintUtils$FingerprintIdentificationState mFpiState;
    private Handler mHandler = new Handler();
    private KeyguardViewMediator mKeyguardViewMediator;
    private boolean mKeyguardVisibility;
    private int mMode;
    private int mPendingAuthenticatedUserId = -1;
    private boolean mPendingShowBouncer;
    private PowerManager mPowerManager;
    private final Runnable mReleaseFingerprintWakeLockRunnable = new Runnable() {
        public void run() {
            Log.i("FingerprintController", "fp wakelock: TIMEOUT!!");
            FingerprintUnlockController.this.releaseFingerprintWakeLock();
        }
    };
    private ScrimController mScrimController;
    private StatusBar mStatusBar;
    private StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    /* access modifiers changed from: private */
    public StatusBarWindowManager mStatusBarWindowManager;
    private final UnlockMethodCache mUnlockMethodCache;
    private KeyguardUpdateMonitor mUpdateMonitor;
    private PowerManager.WakeLock mWakeLock;
    private volatile String mWakingUpReason;

    public FingerprintUnlockController(Context context, DozeScrimController dozeScrimController, KeyguardViewMediator keyguardViewMediator, ScrimController scrimController, StatusBar statusBar, UnlockMethodCache unlockMethodCache) {
        this.mContext = context;
        this.mPowerManager = (PowerManager) context.getSystemService(PowerManager.class);
        this.mUpdateMonitor = KeyguardUpdateMonitor.getInstance(context);
        this.mUpdateMonitor.registerCallback(this);
        this.mStatusBarWindowManager = (StatusBarWindowManager) Dependency.get(StatusBarWindowManager.class);
        this.mDozeScrimController = dozeScrimController;
        this.mKeyguardViewMediator = keyguardViewMediator;
        this.mScrimController = scrimController;
        this.mStatusBar = statusBar;
        this.mUnlockMethodCache = unlockMethodCache;
    }

    public void setStatusBarKeyguardViewManager(StatusBarKeyguardViewManager statusBarKeyguardViewManager) {
        this.mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
    }

    /* access modifiers changed from: private */
    public void releaseFingerprintWakeLock() {
        if (this.mWakeLock != null) {
            this.mHandler.removeCallbacks(this.mReleaseFingerprintWakeLockRunnable);
            Log.i("FingerprintController", "releasing fp wakelock");
            this.mWakeLock.release();
            this.mWakeLock = null;
        }
    }

    public void onFingerprintAcquired(int i) {
        this.mFpiState = MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.ACQUIRED;
        if (i == 0) {
            Trace.beginSection("FingerprintUnlockController#onFingerprintAcquired");
            releaseFingerprintWakeLock();
            if (!this.mUpdateMonitor.isDeviceInteractive()) {
                if (LatencyTracker.isEnabled(this.mContext)) {
                    LatencyTracker.getInstance(this.mContext).onActionStart(2);
                }
                this.mWakeLock = this.mPowerManager.newWakeLock(1, "wake-and-unlock wakelock");
                Trace.beginSection("acquiring wake-and-unlock");
                this.mWakeLock.acquire();
                Trace.endSection();
                Log.i("FingerprintController", "fingerprint acquired, grabbing fp wakelock");
                this.mHandler.postDelayed(this.mReleaseFingerprintWakeLockRunnable, 15000);
                if (this.mDozeScrimController.isPulsing()) {
                    this.mStatusBarWindowManager.setForceDozeBrightness(true);
                }
            }
            Trace.endSection();
        }
    }

    public void onPreFingerprintAuthenticated(int i) {
        Trace.beginSection("FingerprintUnlockController#onPreFingerprintAuthenticated");
        if (this.mUpdateMonitor.isUnlockingWithFingerprintAllowed(i) && KeyguardUpdateMonitor.getCurrentUser() == i && !this.mUpdateMonitor.isBouncerShowing() && this.mKeyguardViewMediator.isShowingAndNotOccluded()) {
            ((MiuiFastUnlockController) Dependency.get(MiuiFastUnlockController.class)).wakeAndFastUnlock("android.policy:FINGERPRINT");
        }
        Trace.endSection();
    }

    public void onKeyguardVisibilityChanged(boolean z) {
        super.onKeyguardVisibilityChanged(z);
        this.mKeyguardVisibility = z;
    }

    public void onFingerprintAuthenticated(int i) {
        int i2;
        Trace.beginSection("FingerprintUnlockController#onFingerprintAuthenticated");
        this.mFpiState = MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.SUCCEEDED;
        int i3 = 0;
        if (!this.mKeyguardViewMediator.isGoingToShowKeyguard() || MiuiKeyguardUtils.isGxzwSensor()) {
            boolean isDeviceInteractive = this.mUpdateMonitor.isDeviceInteractive();
            this.mMode = calculateMode(i);
            if (!(KeyguardUpdateMonitor.getCurrentUser() == i || (i2 = this.mMode) == 3 || i2 == 0 || i2 == 4)) {
                if (MiuiKeyguardUtils.canSwitchUser(this.mContext, i)) {
                    if (MiuiKeyguardUtils.isGxzwSensor()) {
                        MiuiGxzwManager.getInstance().onKeyguardHide();
                    }
                    try {
                        ActivityManagerNative.getDefault().switchUser(i);
                    } catch (RemoteException e) {
                        Log.e("FingerprintController", "switchUser failed", e);
                    }
                } else {
                    this.mMode = 3;
                }
            }
            this.mUpdateMonitor.setFingerprintMode(this.mMode);
            PanelBar.LOG(FingerprintUnlockController.class, "calculateMode userid=" + i + ";mode=" + this.mMode);
            if (!this.mUpdateMonitor.isUnlockingWithFingerprintAllowed(i) && KeyguardUpdateMonitor.getCurrentUser() != i) {
                this.mStatusBarKeyguardViewManager.showPromptReason(this.mKeyguardViewMediator.getBouncerPromptReason(i));
                if (this.mStatusBarKeyguardViewManager.isBouncerShowing()) {
                    this.mStatusBarKeyguardViewManager.applyHintAnimation(500);
                }
            }
            if (!isDeviceInteractive) {
                Log.i("FingerprintController", "fp wakelock: Authenticated, waking up...");
                this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), "android.policy:FINGERPRINT");
            }
            Trace.beginSection("release wake-and-unlock");
            releaseFingerprintWakeLock();
            Trace.endSection();
            int i4 = this.mMode;
            if (i4 == 1 || i4 == 2) {
                if (this.mMode == 2) {
                    Trace.beginSection("MODE_WAKE_AND_UNLOCK_PULSING");
                    this.mStatusBar.updateMediaMetaData(false, true);
                } else {
                    Trace.beginSection("MODE_WAKE_AND_UNLOCK");
                    this.mDozeScrimController.abortDoze();
                }
                keyguardDoneWithoutHomeAnim();
                AnalyticsHelper.getInstance(this.mContext).setWakeupWay("screen_on_by_fp_success");
                this.mStatusBarWindowManager.setStatusBarFocusable(false);
                if (((MiuiFastUnlockController) Dependency.get(MiuiFastUnlockController.class)).isFastUnlock()) {
                    this.mKeyguardViewMediator.keyguardDone();
                } else {
                    this.mKeyguardViewMediator.onWakeAndUnlocking();
                }
                this.mScrimController.setWakeAndUnlocking();
                this.mDozeScrimController.setWakeAndUnlocking();
                if (this.mStatusBar.getNavigationBarView() != null) {
                    this.mStatusBar.getNavigationBarView().setWakeAndUnlocking(true);
                }
                recordUnlockWay();
                Trace.endSection();
            } else if (i4 == 3) {
                Trace.beginSection("MODE_SHOW_BOUNCER");
                if (isDeviceInteractive) {
                    this.mStatusBarKeyguardViewManager.animateCollapsePanels(1.1f);
                } else {
                    this.mStatusBarKeyguardViewManager.notifyDeviceWakeUpRequested();
                    this.mPendingShowBouncer = true;
                }
                Trace.endSection();
            } else if (i4 == 5) {
                Trace.beginSection("MODE_UNLOCK");
                keyguardDoneWithoutHomeAnim();
                if (!isDeviceInteractive) {
                    this.mStatusBarKeyguardViewManager.notifyDeviceWakeUpRequested();
                }
                this.mStatusBarWindowManager.setStatusBarFocusable(false);
                this.mKeyguardViewMediator.keyguardDone();
                recordUnlockWay();
                Trace.endSection();
            } else if (i4 == 6) {
                Trace.beginSection("MODE_DISMISS");
                this.mStatusBarKeyguardViewManager.notifyKeyguardAuthenticated(false);
                recordUnlockWay();
                Trace.endSection();
            }
            if (this.mMode != 2) {
                this.mStatusBarWindowManager.setForceDozeBrightness(false);
            }
            this.mStatusBar.notifyFpAuthModeChanged();
            Trace.endSection();
            return;
        }
        boolean isUnlockingWithFingerprintAllowed = this.mUpdateMonitor.isUnlockingWithFingerprintAllowed(i);
        if (this.mDozeScrimController.isPulsing() && isUnlockingWithFingerprintAllowed) {
            i3 = 2;
        } else if (isUnlockingWithFingerprintAllowed || !this.mUnlockMethodCache.isMethodSecure()) {
            i3 = 1;
        }
        this.mUpdateMonitor.setFingerprintMode(i3);
        if (!this.mKeyguardViewMediator.isShowing() && KeyguardUpdateMonitor.getCurrentUser() == i && (i3 == 2 || i3 == 1)) {
            Slog.i("miui_keyguard_fingerprint", "Unlock by fingerprint, keyguard is not showing and wake up");
            recordUnlockWay();
            this.mKeyguardViewMediator.cancelPendingLock();
            synchronized (this) {
                this.mCancelingPendingLock = true;
            }
            this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), "android.policy:FINGERPRINT");
        } else {
            this.mPendingAuthenticatedUserId = i;
            this.mKeyguardViewMediator.recordFingerprintUnlockState();
        }
        Trace.endSection();
    }

    public void onFingerprintHelp(int i, String str) {
        super.onFingerprintHelp(i, str);
        this.mFpiState = MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.HELP;
    }

    private void keyguardDoneWithoutHomeAnim() {
        MiuiSettings.System.putBooleanForUser(this.mContext.getContentResolver(), "is_fingerprint_unlock", true, -2);
    }

    public void onStartedGoingToSleep(int i) {
        this.mPendingShowBouncer = false;
        this.mPendingAuthenticatedUserId = -1;
        this.mScrimController.resetWakeAndUnlocking();
        if (this.mKeyguardVisibility) {
            int currentUser = KeyguardUpdateMonitor.getCurrentUser();
            if (MiuiKeyguardUtils.isAodEnable(this.mContext) || (MiuiKeyguardUtils.isGxzwSensor() && this.mUpdateMonitor.isUnlockWithFingerprintPossible(currentUser))) {
                this.mScrimController.setAodWaitUnlocking(true);
            }
        }
    }

    public void onFinishedGoingToSleep(int i) {
        Trace.beginSection("FingerprintUnlockController#onFinishedGoingToSleep");
        final int i2 = this.mPendingAuthenticatedUserId;
        if (i2 != -1) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    FingerprintUnlockController.this.onFingerprintAuthenticated(i2);
                }
            });
        }
        int currentUser = KeyguardUpdateMonitor.getCurrentUser();
        if (MiuiKeyguardUtils.isAodEnable(this.mContext) || (MiuiKeyguardUtils.isGxzwSensor() && this.mUpdateMonitor.isUnlockWithFingerprintPossible(currentUser))) {
            this.mScrimController.setAodWaitUnlocking(true);
        }
        this.mPendingAuthenticatedUserId = -1;
        Trace.endSection();
    }

    public int getMode() {
        return this.mMode;
    }

    private int calculateMode(int i) {
        boolean isUnlockingWithFingerprintAllowed = this.mUpdateMonitor.isUnlockingWithFingerprintAllowed(i);
        if (!this.mUpdateMonitor.isDeviceInteractive()) {
            if (!this.mStatusBarKeyguardViewManager.isShowing()) {
                return 4;
            }
            if (this.mDozeScrimController.isPulsing() && isUnlockingWithFingerprintAllowed) {
                return 2;
            }
            if (isUnlockingWithFingerprintAllowed || !this.mUnlockMethodCache.isMethodSecure()) {
                return 1;
            }
            return 3;
        } else if (!this.mStatusBarKeyguardViewManager.isShowing()) {
            return 0;
        } else {
            if (this.mStatusBarKeyguardViewManager.isBouncerShowing() && isUnlockingWithFingerprintAllowed) {
                return 6;
            }
            if (isUnlockingWithFingerprintAllowed) {
                return 5;
            }
            if (!this.mStatusBarKeyguardViewManager.isBouncerShowing()) {
                return 3;
            }
            return 0;
        }
    }

    public void onFingerprintAuthFailed() {
        cleanup();
        this.mFpiState = MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.FAILED;
        AnalyticsHelper.getInstance(this.mContext).recordUnlockWay("fp", false);
    }

    public void onFingerprintError(int i, String str) {
        cleanup();
        if ((("miui.policy:FINGERPRINT_DPAD_CENTER".equals(this.mWakingUpReason) || "android.policy:KEY".equals(this.mWakingUpReason)) || this.mFpiState != MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.NONE) && (i == 7 || i == 9)) {
            if (this.mUpdateMonitor.isDeviceInteractive()) {
                this.mStatusBarKeyguardViewManager.animateCollapsePanels(1.1f);
            } else {
                this.mPendingShowBouncer = true;
                this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), "com.android.systemui:GOTO_UNLOCK");
            }
            this.mWakingUpReason = null;
        }
        this.mFpiState = MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.ERROR;
    }

    public void onFingerprintRunningStateChanged(boolean z) {
        super.onFingerprintRunningStateChanged(z);
        if (z) {
            this.mFpiState = MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.NONE;
        }
    }

    private void cleanup() {
        releaseFingerprintWakeLock();
    }

    public void startKeyguardFadingAway() {
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                FingerprintUnlockController.this.mStatusBarWindowManager.setForceDozeBrightness(false);
            }
        }, 96);
    }

    public void finishKeyguardFadingAway() {
        resetMode();
        this.mStatusBarWindowManager.setForceDozeBrightness(false);
        if (this.mStatusBar.getNavigationBarView() != null) {
            this.mStatusBar.getNavigationBarView().setWakeAndUnlocking(false);
        }
        this.mStatusBar.notifyFpAuthModeChanged();
    }

    public void resetMode() {
        this.mMode = 0;
        this.mUpdateMonitor.setFingerprintMode(this.mMode);
        if (MiuiKeyguardUtils.isGxzwSensor()) {
            MiuiGxzwManager.getInstance().resetGxzwUnlockMode();
        }
    }

    public void onStartedWakingUp() {
        if (this.mPendingShowBouncer) {
            this.mPendingShowBouncer = false;
            this.mStatusBarKeyguardViewManager.animateCollapsePanels(1.1f);
        }
        synchronized (this) {
            if (this.mCancelingPendingLock) {
                this.mCancelingPendingLock = false;
                resetMode();
            }
        }
        this.mScrimController.setAodWaitUnlocking(false);
    }

    private void recordUnlockWay() {
        AnalyticsHelper.getInstance(this.mContext).recordUnlockWay("fp", true);
        this.mKeyguardViewMediator.recordFingerprintUnlockState();
    }

    public synchronized boolean isCancelingPendingLock() {
        return this.mCancelingPendingLock;
    }

    public synchronized void resetCancelingPendingLock() {
        if (this.mCancelingPendingLock) {
            this.mCancelingPendingLock = false;
            this.mHandler.post(new Runnable() {
                public void run() {
                    FingerprintUnlockController.this.resetMode();
                }
            });
        }
    }

    public void onStartedWakingUpReason(String str) {
        this.mWakingUpReason = str;
    }
}
