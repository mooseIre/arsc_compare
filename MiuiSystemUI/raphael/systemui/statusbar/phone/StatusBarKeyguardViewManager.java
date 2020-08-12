package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Trace;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.ViewRootImpl;
import android.view.WindowManagerGlobal;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.LatencyTracker;
import com.android.keyguard.MiuiFastUnlockController;
import com.android.keyguard.ViewMediatorCallback;
import com.android.keyguard.faceunlock.FaceUnlockController;
import com.android.keyguard.faceunlock.FaceUnlockManager;
import com.android.keyguard.wallpaper.MiuiKeyguardWallpaperController;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dependency;
import com.android.systemui.SystemUIFactory;
import com.android.systemui.keyguard.DismissCallbackRegistry;
import com.android.systemui.miui.ActivityObserver;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.RemoteInputController;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import com.android.systemui.statusbar.policy.KeyguardMonitorImpl;
import java.util.ArrayList;

public class StatusBarKeyguardViewManager implements RemoteInputController.Callback {
    private static String TAG = "StatusBarKeyguardViewManager";
    private ActivityStarter.OnDismissAction mAfterKeyguardGoneAction;
    private final ArrayList<Runnable> mAfterKeyguardGoneRunnables = new ArrayList<>();
    private boolean mBackDown;
    private boolean mBackWithVolumeUp = false;
    protected KeyguardBouncer mBouncer;
    /* access modifiers changed from: private */
    public ViewGroup mContainer;
    protected final Context mContext;
    private boolean mDeferScrimFadeOut;
    private boolean mDeviceInteractive = false;
    private boolean mDeviceWillWakeUp;
    private boolean mDozing;
    private FaceUnlockController mFaceUnlockController;
    /* access modifiers changed from: private */
    public FingerprintUnlockController mFingerprintUnlockController;
    protected boolean mFirstUpdate = true;
    private KeyguardMonitorImpl mKeyguardMonitor;
    private boolean mLastBouncerDismissible;
    private boolean mLastBouncerShowing;
    private boolean mLastDeferScrimFadeOut;
    private boolean mLastDozing;
    protected boolean mLastOccluded;
    protected boolean mLastRemoteInputActive;
    protected boolean mLastShowing;
    protected LockPatternUtils mLockPatternUtils;
    private Runnable mMakeNavigationBarVisibleRunnable = new Runnable() {
        public void run() {
            StatusBarKeyguardViewManager.this.mStatusBar.getNavigationBarView().getRootView().setVisibility(0);
        }
    };
    protected boolean mOccluded;
    protected boolean mRemoteInputActive;
    private boolean mScreenTurnedOn;
    /* access modifiers changed from: private */
    public ScrimController mScrimController;
    protected boolean mShowing;
    protected StatusBar mStatusBar;
    /* access modifiers changed from: private */
    public final StatusBarWindowManager mStatusBarWindowManager;
    private final KeyguardUpdateMonitorCallback mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
        public void onEmergencyCallAction() {
            StatusBarKeyguardViewManager statusBarKeyguardViewManager = StatusBarKeyguardViewManager.this;
            if (statusBarKeyguardViewManager.mOccluded) {
                statusBarKeyguardViewManager.reset(false);
            }
        }
    };
    protected ViewMediatorCallback mViewMediatorCallback;

    public void onRemoteInputSent(NotificationData.Entry entry) {
    }

    public StatusBarKeyguardViewManager(Context context, ViewMediatorCallback viewMediatorCallback, LockPatternUtils lockPatternUtils) {
        this.mContext = context;
        this.mViewMediatorCallback = viewMediatorCallback;
        this.mLockPatternUtils = lockPatternUtils;
        this.mStatusBarWindowManager = (StatusBarWindowManager) Dependency.get(StatusBarWindowManager.class);
        this.mKeyguardMonitor = (KeyguardMonitorImpl) Dependency.get(KeyguardMonitor.class);
        KeyguardUpdateMonitor.getInstance(context).registerCallback(this.mUpdateMonitorCallback);
    }

    public void registerStatusBar(StatusBar statusBar, ViewGroup viewGroup, ScrimController scrimController, FingerprintUnlockController fingerprintUnlockController, FaceUnlockController faceUnlockController, DismissCallbackRegistry dismissCallbackRegistry) {
        this.mStatusBar = statusBar;
        this.mContainer = viewGroup;
        this.mScrimController = scrimController;
        this.mFingerprintUnlockController = fingerprintUnlockController;
        this.mFaceUnlockController = faceUnlockController;
        this.mBouncer = SystemUIFactory.getInstance().createKeyguardBouncer(this.mContext, this.mViewMediatorCallback, this.mLockPatternUtils, viewGroup, dismissCallbackRegistry);
    }

    public void setKeyguardTransparent() {
        this.mStatusBar.setKeyguardTransparent();
    }

    public void show(Bundle bundle) {
        this.mShowing = true;
        this.mStatusBarWindowManager.setKeygaurdTransparent(false);
        this.mStatusBarWindowManager.setKeyguardShowing(true);
        this.mScrimController.abortKeyguardFadingOut();
        reset(true);
    }

    /* access modifiers changed from: protected */
    public void showBouncerOrKeyguard(boolean z) {
        if (this.mBouncer.needsFullscreenBouncer()) {
            this.mStatusBar.hideKeyguard();
            this.mBouncer.show(true);
            return;
        }
        this.mStatusBar.showKeyguard();
        if (z) {
            this.mBouncer.hide(false);
            this.mBouncer.prepare();
        }
    }

    private void showBouncer() {
        if (this.mShowing) {
            this.mBouncer.show(false);
        }
        updateStates();
    }

    public void dismissWithAction(ActivityStarter.OnDismissAction onDismissAction, Runnable runnable, boolean z) {
        if (this.mShowing) {
            if (!z) {
                this.mBouncer.showWithDismissAction(onDismissAction, runnable);
            } else {
                this.mAfterKeyguardGoneAction = onDismissAction;
                this.mBouncer.show(false);
            }
        }
        updateStates();
    }

    public void addAfterKeyguardGoneRunnable(Runnable runnable) {
        this.mAfterKeyguardGoneRunnables.add(runnable);
    }

    public void reset(boolean z) {
        if (this.mShowing) {
            if (this.mOccluded) {
                this.mStatusBar.hideKeyguard();
                this.mStatusBar.stopWaitingForKeyguardExit();
                this.mBouncer.hide(false);
                this.mBouncer.prepare();
            } else {
                showBouncerOrKeyguard(z);
            }
            KeyguardUpdateMonitor.getInstance(this.mContext).sendKeyguardReset();
            updateStates();
        }
    }

    public void onStartedGoingToSleep() {
        this.mStatusBar.onStartedGoingToSleep();
        this.mStatusBarWindowManager.setKeygaurdTransparent(false);
    }

    public void onFinishedGoingToSleep() {
        this.mDeviceInteractive = false;
        this.mStatusBar.onFinishedGoingToSleep();
        this.mBouncer.onFinishedGoingToSleep();
    }

    public void onStartedWakingUp() {
        Trace.beginSection("StatusBarKeyguardViewManager#onStartedWakingUp");
        this.mDeviceInteractive = true;
        this.mDeviceWillWakeUp = false;
        this.mStatusBar.onStartedWakingUp();
        Trace.endSection();
    }

    public void onScreenTurningOn() {
        Trace.beginSection("StatusBarKeyguardViewManager#onScreenTurningOn");
        this.mStatusBar.onScreenTurningOn();
        Trace.endSection();
    }

    public boolean isScreenTurnedOn() {
        return this.mScreenTurnedOn;
    }

    public void onScreenTurnedOn() {
        Trace.beginSection("StatusBarKeyguardViewManager#onScreenTurnedOn");
        this.mScreenTurnedOn = true;
        if (this.mDeferScrimFadeOut) {
            this.mDeferScrimFadeOut = false;
            animateScrimControllerKeyguardFadingOut(0, 0, true);
            updateStates();
        }
        this.mStatusBar.onScreenTurnedOn();
        Trace.endSection();
    }

    public void onRemoteInputActive(boolean z) {
        this.mRemoteInputActive = z;
        updateStates();
    }

    public void setDozing(boolean z) {
        this.mDozing = z;
        updateStates();
    }

    public void onScreenTurnedOff() {
        this.mScreenTurnedOn = false;
        this.mStatusBar.onScreenTurnedOff();
    }

    public void notifyDeviceWakeUpRequested() {
        this.mDeviceWillWakeUp = !this.mDeviceInteractive;
    }

    public void setNeedsInput(boolean z) {
        this.mStatusBarWindowManager.setKeyguardNeedsInput(z);
    }

    public void setOccluded(boolean z, boolean z2) {
        if (z != this.mOccluded) {
            this.mStatusBar.onKeyguardOccludedChanged(z);
        }
        boolean z3 = true;
        if (!z || this.mOccluded || !this.mShowing || !this.mStatusBar.isInLaunchTransition()) {
            this.mOccluded = z;
            if (this.mShowing) {
                StatusBar statusBar = this.mStatusBar;
                if (!z2 || z) {
                    z3 = false;
                }
                statusBar.updateMediaMetaData(false, z3);
            }
            this.mStatusBarWindowManager.setKeyguardOccluded(z);
            reset(false);
            return;
        }
        this.mOccluded = true;
        this.mStatusBar.fadeKeyguardAfterLaunchTransition((Runnable) null, new Runnable() {
            public void run() {
                StatusBarKeyguardViewManager.this.mStatusBarWindowManager.setKeyguardOccluded(StatusBarKeyguardViewManager.this.mOccluded);
                StatusBarKeyguardViewManager.this.reset(true);
            }
        });
    }

    public boolean isOccluded() {
        return this.mOccluded;
    }

    public void startPreHideAnimation(Runnable runnable) {
        if (this.mBouncer.isShowing()) {
            this.mBouncer.startPreHideAnimation(runnable);
        } else if (runnable != null) {
            runnable.run();
        }
    }

    public void hide(long j, long j2) {
        long j3;
        this.mShowing = false;
        long max = Math.max(0, (j - 48) - SystemClock.uptimeMillis());
        this.mStatusBar.onKeyguardDone();
        if (this.mStatusBar.isInLaunchTransition()) {
            this.mStatusBar.fadeKeyguardAfterLaunchTransition(new Runnable() {
                public void run() {
                    StatusBarKeyguardViewManager.this.mStatusBarWindowManager.setKeyguardShowing(false);
                    StatusBarKeyguardViewManager.this.mStatusBarWindowManager.setKeyguardFadingAway(true);
                    StatusBarKeyguardViewManager.this.mBouncer.hide(true);
                    StatusBarKeyguardViewManager.this.updateStates();
                    StatusBarKeyguardViewManager.this.mScrimController.animateKeyguardFadingOut(100, 300, (Runnable) null, false);
                }
            }, new Runnable() {
                public void run() {
                    StatusBarKeyguardViewManager.this.mStatusBar.hideKeyguard();
                    StatusBarKeyguardViewManager.this.mStatusBarWindowManager.setKeyguardFadingAway(false);
                    StatusBarKeyguardViewManager.this.mViewMediatorCallback.keyguardGone();
                    StatusBarKeyguardViewManager.this.executeAfterKeyguardGoneAction();
                    StatusBarKeyguardViewManager.this.mFingerprintUnlockController.resetMode();
                }
            });
            return;
        }
        executeAfterKeyguardGoneAction();
        boolean z = this.mFingerprintUnlockController.getMode() == 2;
        boolean z2 = this.mFingerprintUnlockController.getMode() == 5;
        boolean isLegacyKeyguardWallpaper = ((MiuiKeyguardWallpaperController) Dependency.get(MiuiKeyguardWallpaperController.class)).isLegacyKeyguardWallpaper();
        boolean isTopActivityLauncher = ((ActivityObserver) Dependency.get(ActivityObserver.class)).isTopActivityLauncher();
        if (z) {
            j3 = 240;
            max = 0;
        } else {
            j3 = j2;
        }
        this.mStatusBar.setKeyguardFadingAway(j, max, j3);
        this.mFingerprintUnlockController.startKeyguardFadingAway();
        this.mBouncer.hide(true);
        if (z) {
            this.mStatusBarWindowManager.setKeyguardFadingAway(true);
            this.mStatusBar.fadeKeyguardWhilePulsing();
            animateScrimControllerKeyguardFadingOut(max, j3, new Runnable() {
                public void run() {
                    StatusBarKeyguardViewManager.this.mStatusBar.hideKeyguard();
                }
            }, false);
        } else {
            long j4 = j3;
            if (!z2 || this.mOccluded || ((MiuiFastUnlockController) Dependency.get(MiuiFastUnlockController.class)).isFastUnlock() || isLegacyKeyguardWallpaper || !isTopActivityLauncher) {
                this.mFingerprintUnlockController.startKeyguardFadingAway();
                long j5 = j4;
                this.mStatusBar.setKeyguardFadingAway(j, max, j4);
                if (!this.mStatusBar.hideKeyguard()) {
                    this.mStatusBarWindowManager.setKeyguardFadingAway(true);
                    if (this.mFingerprintUnlockController.getMode() == 1) {
                        animateScrimControllerKeyguardFadingOut(0, 0, true);
                    } else {
                        animateScrimControllerKeyguardFadingOut(max, j5, false);
                    }
                } else {
                    this.mScrimController.animateGoingToFullShade(max, j5);
                    this.mStatusBar.finishKeyguardFadingAway();
                    this.mFingerprintUnlockController.finishKeyguardFadingAway();
                }
            } else {
                this.mStatusBarWindowManager.setKeyguardFadingAway(true);
                this.mStatusBar.setKeyguardFadingAway(j, 350, 0);
                this.mStatusBar.fadeKeyguardWhenUnlockByFingerprint(new Runnable() {
                    public final void run() {
                        StatusBarKeyguardViewManager.this.lambda$hide$0$StatusBarKeyguardViewManager();
                    }
                });
            }
        }
        updateStates();
        this.mStatusBarWindowManager.setKeyguardShowing(false);
        this.mViewMediatorCallback.keyguardGone();
    }

    public /* synthetic */ void lambda$hide$0$StatusBarKeyguardViewManager() {
        this.mStatusBar.hideKeyguard();
        animateScrimControllerKeyguardFadingOut(0, 0, false);
    }

    public void onDensityOrFontScaleChanged() {
        this.mBouncer.hide(true);
    }

    private void animateScrimControllerKeyguardFadingOut(long j, long j2, boolean z) {
        animateScrimControllerKeyguardFadingOut(j, j2, (Runnable) null, z);
    }

    private void animateScrimControllerKeyguardFadingOut(long j, long j2, Runnable runnable, boolean z) {
        Trace.asyncTraceBegin(8, "Fading out", 0);
        final Runnable runnable2 = runnable;
        this.mScrimController.animateKeyguardFadingOut(j, j2, new Runnable() {
            public void run() {
                Runnable runnable = runnable2;
                if (runnable != null) {
                    runnable.run();
                }
                StatusBarKeyguardViewManager.this.mContainer.postDelayed(new Runnable() {
                    public void run() {
                        StatusBarKeyguardViewManager.this.mStatusBarWindowManager.setKeyguardFadingAway(false);
                    }
                }, 100);
                StatusBarKeyguardViewManager.this.mStatusBar.finishKeyguardFadingAway();
                StatusBarKeyguardViewManager.this.mFingerprintUnlockController.finishKeyguardFadingAway();
                WindowManagerGlobal.getInstance().trimMemory(20);
                Trace.asyncTraceEnd(8, "Fading out", 0);
            }
        }, z);
        if (this.mFingerprintUnlockController.getMode() == 1 && LatencyTracker.isEnabled(this.mContext)) {
            DejankUtils.postAfterTraversal(new Runnable() {
                public void run() {
                    LatencyTracker.getInstance(StatusBarKeyguardViewManager.this.mContext).onActionEnd(2);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public void executeAfterKeyguardGoneAction() {
        ActivityStarter.OnDismissAction onDismissAction = this.mAfterKeyguardGoneAction;
        if (onDismissAction != null) {
            onDismissAction.onDismiss();
            this.mAfterKeyguardGoneAction = null;
        }
        for (int i = 0; i < this.mAfterKeyguardGoneRunnables.size(); i++) {
            this.mAfterKeyguardGoneRunnables.get(i).run();
        }
        this.mAfterKeyguardGoneRunnables.clear();
    }

    public void dismissAndCollapse() {
        this.mStatusBar.executeRunnableDismissingKeyguard((Runnable) null, (Runnable) null, true, false, true);
    }

    public void dismiss() {
        showBouncer();
    }

    public boolean isSecure() {
        return this.mBouncer.isSecure();
    }

    public boolean isShowing() {
        return this.mShowing;
    }

    public boolean onBackPressed() {
        if (!this.mBouncer.isShowing()) {
            return false;
        }
        if (this.mBouncer.onBackPressed()) {
            return true;
        }
        this.mStatusBar.endAffordanceLaunch();
        reset(true);
        return true;
    }

    public boolean isBouncerShowing() {
        return this.mBouncer.isShowing();
    }

    private long getNavBarShowDelay() {
        if (this.mStatusBar.isKeyguardFadingAway()) {
            return this.mStatusBar.getKeyguardFadingAwayDelay();
        }
        return this.mBouncer.isShowing() ? 320 : 0;
    }

    /* access modifiers changed from: protected */
    public void updateStates() {
        int systemUiVisibility = this.mContainer.getSystemUiVisibility();
        boolean z = this.mShowing;
        boolean z2 = this.mOccluded;
        boolean isShowing = this.mBouncer.isShowing();
        boolean z3 = true;
        boolean z4 = !this.mBouncer.isFullscreenBouncer();
        boolean z5 = this.mRemoteInputActive;
        if ((z4 || !z || z5) != (this.mLastBouncerDismissible || !this.mLastShowing || this.mLastRemoteInputActive) || this.mFirstUpdate) {
            if (z4 || !z || z5) {
                this.mContainer.setSystemUiVisibility(systemUiVisibility & -4194305);
            } else {
                this.mContainer.setSystemUiVisibility(systemUiVisibility | 4194304);
            }
        }
        boolean isNavBarVisible = isNavBarVisible();
        if ((isNavBarVisible != getLastNavBarVisible() || this.mFirstUpdate) && this.mStatusBar.getNavigationBarView() != null) {
            if (isNavBarVisible) {
                long navBarShowDelay = getNavBarShowDelay();
                if (navBarShowDelay == 0) {
                    this.mMakeNavigationBarVisibleRunnable.run();
                } else {
                    this.mContainer.postOnAnimationDelayed(this.mMakeNavigationBarVisibleRunnable, navBarShowDelay);
                }
            } else {
                this.mContainer.removeCallbacks(this.mMakeNavigationBarVisibleRunnable);
                this.mStatusBar.getNavigationBarView().getRootView().setVisibility(8);
            }
        }
        if (isShowing != this.mLastBouncerShowing || this.mFirstUpdate) {
            this.mStatusBarWindowManager.setBouncerShowing(isShowing);
            this.mStatusBar.setBouncerShowing(isShowing);
            this.mScrimController.setBouncerShowing(isShowing);
        }
        KeyguardUpdateMonitor instance = KeyguardUpdateMonitor.getInstance(this.mContext);
        if ((z && !z2) != (this.mLastShowing && !this.mLastOccluded) || this.mFirstUpdate) {
            if (!z || z2) {
                z3 = false;
            }
            instance.onKeyguardVisibilityChanged(z3);
        }
        if (isShowing != this.mLastBouncerShowing || this.mFirstUpdate) {
            instance.sendKeyguardBouncerChanged(isShowing);
        }
        this.mFirstUpdate = false;
        this.mLastShowing = z;
        this.mLastOccluded = z2;
        this.mLastBouncerShowing = isShowing;
        this.mLastBouncerDismissible = z4;
        this.mLastRemoteInputActive = z5;
        this.mLastDeferScrimFadeOut = this.mDeferScrimFadeOut;
        this.mLastDozing = this.mDozing;
        this.mStatusBar.onKeyguardViewManagerStatesUpdated();
    }

    /* access modifiers changed from: protected */
    public boolean isNavBarVisible() {
        return (((!this.mShowing || this.mOccluded) && !this.mDozing) || this.mBouncer.isShowing() || this.mRemoteInputActive) && !this.mDeferScrimFadeOut;
    }

    /* access modifiers changed from: protected */
    public boolean getLastNavBarVisible() {
        return (((!this.mLastShowing || this.mLastOccluded) && !this.mLastDozing) || this.mLastBouncerShowing || this.mLastRemoteInputActive) && !this.mLastDeferScrimFadeOut;
    }

    public boolean shouldDismissOnMenuPressed() {
        return this.mBouncer.shouldDismissOnMenuPressed();
    }

    public boolean interceptMediaKey(KeyEvent keyEvent) {
        if (interceptKey(keyEvent)) {
            return true;
        }
        return this.mBouncer.interceptMediaKey(keyEvent);
    }

    /* JADX INFO: finally extract failed */
    public boolean interceptKey(KeyEvent keyEvent) {
        int keyCode = keyEvent.getKeyCode();
        if (keyEvent.getAction() == 0) {
            if (keyCode == 4) {
                this.mBackDown = true;
                setBackWithVolumeUp(false);
            } else if (keyCode == 24) {
                setBackWithVolumeUp(this.mBackDown);
            } else {
                this.mBackDown = false;
            }
        } else if (keyEvent.getAction() == 1) {
            if (keyCode == 24) {
                try {
                    if (this.mBackDown) {
                        this.mBackDown = false;
                        dismissAndCollapse();
                        Log.d(TAG, "Unlock Screen by pressing back + volume_up");
                        this.mBackDown = false;
                        return true;
                    }
                } catch (Throwable th) {
                    this.mBackDown = false;
                    throw th;
                }
            } else if (keyCode == 4) {
                if (this.mBackWithVolumeUp) {
                    setBackWithVolumeUp(false);
                    this.mBackDown = false;
                    return true;
                }
                this.mBackDown = false;
                return false;
            }
            this.mBackDown = false;
        }
        return false;
    }

    private void setBackWithVolumeUp(boolean z) {
        if (this.mBackWithVolumeUp != z) {
            this.mBackWithVolumeUp = z;
            this.mKeyguardMonitor.notifySkipVolumeDialog(this.mBackWithVolumeUp);
        }
    }

    public void readyForKeyguardDone() {
        this.mViewMediatorCallback.readyForKeyguardDone();
    }

    public boolean isSecure(int i) {
        boolean z = this.mBouncer.isSecure() || UnlockMethodCache.getInstance(this.mContext).isMethodSecure(i);
        KeyguardUpdateMonitor instance = KeyguardUpdateMonitor.getInstance(this.mContext);
        if (i != 0 || !FaceUnlockManager.getInstance().isShowMessageWhenFaceUnlockSuccess()) {
            return z;
        }
        return z && !instance.isFaceUnlock();
    }

    public void keyguardGoingAway() {
        this.mStatusBar.keyguardGoingAway();
    }

    public void animateCollapsePanels(float f) {
        this.mStatusBar.animateCollapsePanels(0, true, false, f);
    }

    public void notifyKeyguardAuthenticated(boolean z) {
        this.mBouncer.notifyKeyguardAuthenticated(z);
    }

    public void showBouncerMessage(String str, int i) {
        this.mBouncer.showMessage(str, i);
    }

    public void showBouncerMessage(String str, String str2, int i) {
        this.mBouncer.showMessage(str, str2, i);
    }

    public void showPromptReason(int i) {
        this.mBouncer.showPromptReason(i);
    }

    public void applyHintAnimation(long j) {
        this.mBouncer.applyHintAnimation(j);
    }

    public ViewRootImpl getViewRootImpl() {
        return this.mStatusBar.getStatusBarView().getViewRootImpl();
    }
}
