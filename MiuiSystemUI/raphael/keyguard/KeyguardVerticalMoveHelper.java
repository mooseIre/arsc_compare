package com.android.keyguard;

import android.animation.ValueAnimator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import com.android.keyguard.faceunlock.MiuiKeyguardFaceUnlockView;
import com.android.keyguard.injector.KeyguardUpdateMonitorInjector;
import com.android.keyguard.wallpaper.KeyguardWallpaperUtils;
import com.android.keyguard.wallpaper.WallpaperCommandSender;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController;

public class KeyguardVerticalMoveHelper extends BaseKeyguardMoveHelper {
    private MiuiKeyguardFaceUnlockView mFaceUnlockView;
    private boolean mIsPerformingTouchEvent;

    public KeyguardVerticalMoveHelper(MiuiNotificationPanelViewController miuiNotificationPanelViewController) {
        this.mNotificationPanelViewController = miuiNotificationPanelViewController;
        this.mFaceUnlockView = miuiNotificationPanelViewController.getKeyguardFaceUnlockView();
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0 || this.mIsPerformingTouchEvent) {
            initCommonTouchEvent(motionEvent);
            if (actionMasked != 0) {
                if (actionMasked != 1) {
                    if (actionMasked == 2) {
                        float f = this.mInitialTouchY - this.y;
                        if (f < 0.0f) {
                            f = 0.0f;
                        }
                        if (this.mNotificationPanelViewController.isOnKeyguard()) {
                            float min = Math.min(f / ((float) (this.mNotificationPanelViewController.getHeight() / 2)), 1.0f);
                            this.mTranslationPer = min;
                            doPanelViewAnimation();
                            if (KeyguardWallpaperUtils.isWallpaperShouldBlur()) {
                                ((WallpaperCommandSender) Dependency.get(WallpaperCommandSender.class)).updateKeyguardRatio(min - 4.0f, 0);
                            }
                        }
                    } else if (actionMasked != 3) {
                        if (actionMasked == 5) {
                            endMotionEvent(motionEvent, this.x, this.y, true);
                            return false;
                        }
                    }
                }
                this.mIsPerformingTouchEvent = false;
                endMotionEvent(motionEvent, this.x, this.y, false);
            } else {
                this.mIsPerformingTouchEvent = true;
            }
            return true;
        }
        Log.d("KeyguardVerticalMoveHelper", " onTouchEvent is intercepted");
        return false;
    }

    private void endMotionEvent(MotionEvent motionEvent, float f, float f2, boolean z) {
        float f3;
        VelocityTracker velocityTracker = this.mVelocityTracker;
        float f4 = 0.0f;
        if (velocityTracker != null) {
            velocityTracker.computeCurrentVelocity(1000);
            f4 = this.mVelocityTracker.getYVelocity();
            f3 = (float) Math.hypot((double) this.mVelocityTracker.getXVelocity(), (double) this.mVelocityTracker.getYVelocity());
        } else {
            f3 = 0.0f;
        }
        if (this.mNotificationPanelViewController.flingExpands(f4, f3, f, f2) || motionEvent.getActionMasked() == 3 || z) {
            this.mFaceUnlockView.updateFaceUnlockIconStatus();
            showOrHideKeyguard(true);
            if (((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).isKeyguardShowing()) {
                ((WallpaperCommandSender) Dependency.get(WallpaperCommandSender.class)).updateKeyguardRatio(-1.0f, 300);
            }
        } else {
            showOrHideKeyguard(false);
        }
        VelocityTracker velocityTracker2 = this.mVelocityTracker;
        if (velocityTracker2 != null) {
            velocityTracker2.recycle();
            this.mVelocityTracker = null;
        }
    }

    public void initDownStates(MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() == 0) {
            onTouchEvent(motionEvent);
        }
    }

    public void reset() {
        ValueAnimator valueAnimator = this.mAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        showOrHideKeyguard(true);
    }
}
