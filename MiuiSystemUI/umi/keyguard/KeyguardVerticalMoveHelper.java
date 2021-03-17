package com.android.keyguard;

import android.animation.ValueAnimator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.animation.DecelerateInterpolator;
import com.android.keyguard.faceunlock.MiuiKeyguardFaceUnlockView;
import com.android.keyguard.injector.KeyguardPanelViewInjector;
import com.android.keyguard.injector.KeyguardUpdateMonitorInjector;
import com.android.keyguard.wallpaper.KeyguardWallpaperUtils;
import com.android.keyguard.wallpaper.WallpaperCommandSender;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController;

public class KeyguardVerticalMoveHelper {
    private ValueAnimator mAnimator;
    private MiuiKeyguardFaceUnlockView mFaceUnlockView;
    private float mInitialTouchY;
    private boolean mIsPerformingTouchEvent;
    private MiuiNotificationPanelViewController mNotificationPanelViewController;
    private float mTranslationPer;
    private VelocityTracker mVelocityTracker;

    public KeyguardVerticalMoveHelper(MiuiNotificationPanelViewController miuiNotificationPanelViewController) {
        this.mNotificationPanelViewController = miuiNotificationPanelViewController;
        this.mFaceUnlockView = miuiNotificationPanelViewController.getKeyguardFaceUnlockView();
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        float y = motionEvent.getY();
        float x = motionEvent.getX();
        if (actionMasked == 0 || this.mIsPerformingTouchEvent) {
            if (actionMasked != 0) {
                if (actionMasked != 1) {
                    if (actionMasked == 2) {
                        trackMovement(motionEvent);
                        float f = this.mInitialTouchY - y;
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
                            endMotionEvent(motionEvent, x, y, true);
                            return false;
                        }
                    }
                }
                trackMovement(motionEvent);
                this.mIsPerformingTouchEvent = false;
                endMotionEvent(motionEvent, x, y, false);
            } else {
                this.mInitialTouchY = y;
                this.mIsPerformingTouchEvent = true;
                initVelocityTracker();
                trackMovement(motionEvent);
            }
            return true;
        }
        Log.d("KeyguardVerticalMoveHelper", " onTouchEvent is intercepted");
        return false;
    }

    private void initVelocityTracker() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.recycle();
        }
        this.mVelocityTracker = VelocityTracker.obtain();
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

    public void reset() {
        ValueAnimator valueAnimator = this.mAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        showOrHideKeyguard(true);
    }

    private void showOrHideKeyguard(boolean z) {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(this.mTranslationPer, z ? 0.0f : 1.0f);
        this.mAnimator = ofFloat;
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.keyguard.$$Lambda$KeyguardVerticalMoveHelper$5v4VkKBCU8GOUWyenO__QuyEhVI */

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                KeyguardVerticalMoveHelper.this.lambda$showOrHideKeyguard$0$KeyguardVerticalMoveHelper(valueAnimator);
            }
        });
        this.mAnimator.setDuration(300L);
        this.mAnimator.setInterpolator(new DecelerateInterpolator());
        this.mAnimator.start();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showOrHideKeyguard$0 */
    public /* synthetic */ void lambda$showOrHideKeyguard$0$KeyguardVerticalMoveHelper(ValueAnimator valueAnimator) {
        this.mTranslationPer = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        doPanelViewAnimation();
    }

    private void doPanelViewAnimation() {
        float f = this.mTranslationPer;
        float f2 = 1.0f - ((1.0f - f) * (1.0f - f));
        if (!((KeyguardPanelViewInjector) Dependency.get(KeyguardPanelViewInjector.class)).isForceBlack()) {
            float f3 = 1.0f - (0.1f * f2);
            this.mNotificationPanelViewController.getPanelView().setScaleX(checkIsNaN(f3));
            this.mNotificationPanelViewController.getPanelView().setScaleY(checkIsNaN(f3));
        }
        this.mNotificationPanelViewController.getPanelView().setTransitionAlpha(checkIsNaN(1.0f - f2));
    }

    private float checkIsNaN(float f) {
        if (!Float.isNaN(f)) {
            return f;
        }
        Log.e("KeyguardVerticalMoveHelper", " mTranslationPer:" + this.mTranslationPer);
        return 1.0f;
    }

    private void trackMovement(MotionEvent motionEvent) {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.addMovement(motionEvent);
        }
    }
}
