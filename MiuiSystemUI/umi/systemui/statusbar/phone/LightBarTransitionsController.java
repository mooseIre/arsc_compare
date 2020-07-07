package com.android.systemui.statusbar.phone;

import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.TimeUtils;
import com.android.internal.os.SomeArgs;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.Interpolators;
import com.android.systemui.SystemUI;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class LightBarTransitionsController implements Dumpable, CommandQueue.Callbacks {
    private final DarkIntensityApplier mApplier;
    private float mDarkIntensity;
    private final Handler mHandler;
    private boolean mIsDark;
    private final KeyguardMonitor mKeyguardMonitor;
    private float mNextDarkIntensity;
    private float mPendingDarkIntensity;
    private ValueAnimator mTintAnimator;
    private boolean mTintChangePending;
    /* access modifiers changed from: private */
    public boolean mTransitionDeferring;
    private final Runnable mTransitionDeferringDoneRunnable = new Runnable() {
        public void run() {
            boolean unused = LightBarTransitionsController.this.mTransitionDeferring = false;
        }
    };
    private long mTransitionDeferringDuration;
    private long mTransitionDeferringStartTime;
    private boolean mTransitionPending;
    private boolean mUseTint;

    public interface DarkIntensityApplier {
        void applyDarkIntensity(float f);
    }

    public void addQsTile(ComponentName componentName) {
    }

    public void animateCollapsePanels(int i) {
    }

    public void animateExpandNotificationsPanel() {
    }

    public void animateExpandSettingsPanel(String str) {
    }

    public void appTransitionFinished() {
    }

    public void cancelPreloadRecentApps() {
    }

    public void clickTile(ComponentName componentName) {
    }

    public void disable(int i, int i2, boolean z) {
    }

    public void dismissKeyboardShortcutsMenu() {
    }

    public void handleShowGlobalActionsMenu() {
    }

    public void handleSystemNavigationKey(int i) {
    }

    public void hideFingerprintDialog() {
    }

    public void hideRecentApps(boolean z, boolean z2) {
    }

    public void onFingerprintAuthenticated() {
    }

    public void onFingerprintError(String str) {
    }

    public void onFingerprintHelp(String str) {
    }

    public void preloadRecentApps() {
    }

    public void remQsTile(ComponentName componentName) {
    }

    public void removeIcon(String str) {
    }

    public void setIcon(String str, StatusBarIcon statusBarIcon) {
    }

    public void setImeWindowStatus(IBinder iBinder, int i, int i2, boolean z) {
    }

    public void setStatus(int i, String str, Bundle bundle) {
    }

    public void setSystemUiVisibility(int i, int i2, int i3, int i4, Rect rect, Rect rect2) {
    }

    public void setWindowState(int i, int i2) {
    }

    public void showAssistDisclosure() {
    }

    public void showFingerprintDialog(SomeArgs someArgs) {
    }

    public void showPictureInPictureMenu() {
    }

    public void showRecentApps(boolean z, boolean z2) {
    }

    public void showScreenPinningRequest(int i) {
    }

    public void startAssist(Bundle bundle) {
    }

    public void toggleKeyboardShortcutsMenu(int i) {
    }

    public void toggleRecentApps() {
    }

    public void toggleSplitScreen() {
    }

    public void topAppWindowChanged(boolean z) {
    }

    public LightBarTransitionsController(Context context, DarkIntensityApplier darkIntensityApplier) {
        this.mApplier = darkIntensityApplier;
        this.mHandler = new Handler();
        this.mKeyguardMonitor = (KeyguardMonitor) Dependency.get(KeyguardMonitor.class);
        ((CommandQueue) SystemUI.getComponent(context, CommandQueue.class)).addCallbacks(this);
    }

    public void appTransitionPending(boolean z) {
        if (!this.mKeyguardMonitor.isKeyguardGoingAway() || z) {
            this.mTransitionPending = true;
        }
    }

    public void appTransitionCancelled() {
        if (this.mTransitionPending && this.mTintChangePending) {
            this.mTintChangePending = false;
            animateIconTint(this.mPendingDarkIntensity, 0, this.mUseTint ? 500 : 120);
        }
        this.mTransitionPending = false;
    }

    public void appTransitionStarting(long j, long j2, boolean z) {
        if (!this.mKeyguardMonitor.isKeyguardGoingAway() || z) {
            if (this.mTransitionPending && this.mTintChangePending) {
                this.mTintChangePending = false;
                animateIconTint(this.mPendingDarkIntensity, Math.max(0, j - SystemClock.uptimeMillis()), j2);
            } else if (this.mTransitionPending) {
                this.mTransitionDeferring = true;
                this.mTransitionDeferringStartTime = j;
                this.mTransitionDeferringDuration = j2;
                this.mHandler.removeCallbacks(this.mTransitionDeferringDoneRunnable);
                this.mHandler.postAtTime(this.mTransitionDeferringDoneRunnable, j);
            }
            this.mTransitionPending = false;
        }
    }

    public void setIconsDark(boolean z, boolean z2) {
        if (this.mIsDark != z) {
            this.mIsDark = z;
            float f = 1.0f;
            if (!z2) {
                ValueAnimator valueAnimator = this.mTintAnimator;
                if (valueAnimator != null) {
                    valueAnimator.cancel();
                }
                setIconTintInternal(z ? 1.0f : 0.0f);
                if (!z) {
                    f = 0.0f;
                }
                this.mNextDarkIntensity = f;
            } else if (this.mTransitionPending) {
                if (!z) {
                    f = 0.0f;
                }
                deferIconTintChange(f);
            } else if (this.mTransitionDeferring) {
                animateIconTint(z ? 1.0f : 0.0f, Math.max(0, this.mTransitionDeferringStartTime - SystemClock.uptimeMillis()), this.mTransitionDeferringDuration);
            } else {
                animateIconTint(z ? 1.0f : 0.0f, 0, this.mUseTint ? 500 : 120);
            }
        }
    }

    public void setUseTint(boolean z) {
        this.mUseTint = z;
    }

    private void deferIconTintChange(float f) {
        if (!this.mTintChangePending || f != this.mPendingDarkIntensity) {
            this.mTintChangePending = true;
            this.mPendingDarkIntensity = f;
        }
    }

    private void animateIconTint(float f, long j, long j2) {
        if (this.mNextDarkIntensity != f) {
            ValueAnimator valueAnimator = this.mTintAnimator;
            if (valueAnimator != null) {
                valueAnimator.cancel();
            }
            this.mNextDarkIntensity = f;
            ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{this.mDarkIntensity, f});
            this.mTintAnimator = ofFloat;
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    LightBarTransitionsController.this.setIconTintInternal(((Float) valueAnimator.getAnimatedValue()).floatValue());
                }
            });
            this.mTintAnimator.setDuration(j2);
            this.mTintAnimator.setStartDelay(j);
            this.mTintAnimator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            this.mTintAnimator.start();
        }
    }

    /* access modifiers changed from: private */
    public void setIconTintInternal(float f) {
        this.mDarkIntensity = f;
        this.mApplier.applyDarkIntensity(f);
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.print("  mTransitionDeferring=");
        printWriter.print(this.mTransitionDeferring);
        if (this.mTransitionDeferring) {
            printWriter.println();
            printWriter.print("   mTransitionDeferringStartTime=");
            printWriter.println(TimeUtils.formatUptime(this.mTransitionDeferringStartTime));
            printWriter.print("   mTransitionDeferringDuration=");
            TimeUtils.formatDuration(this.mTransitionDeferringDuration, printWriter);
            printWriter.println();
        }
        printWriter.print("  mTransitionPending=");
        printWriter.print(this.mTransitionPending);
        printWriter.print(" mTintChangePending=");
        printWriter.println(this.mTintChangePending);
        printWriter.print("  mPendingDarkIntensity=");
        printWriter.print(this.mPendingDarkIntensity);
        printWriter.print(" mDarkIntensity=");
        printWriter.print(this.mDarkIntensity);
        printWriter.print(" mNextDarkIntensity=");
        printWriter.println(this.mNextDarkIntensity);
    }
}
