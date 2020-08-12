package com.android.systemui.statusbar.phone;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.MiuiKeyguardFaceUnlockView;
import com.android.keyguard.magazine.LockScreenMagazinePreView;
import com.android.keyguard.wallpaper.KeyguardWallpaperUtils;
import com.android.keyguard.wallpaper.MiuiKeyguardWallpaperController;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.R;

public class KeyguardVerticalMoveHelper {
    private ValueAnimator mAnimator;
    private final Context mContext;
    private MiuiKeyguardFaceUnlockView mFaceUnlockView;
    private long mInitialDownTime;
    private float mInitialTouchX;
    private float mInitialTouchY;
    /* access modifiers changed from: private */
    public boolean mIsLockScreenMagazinePreViewVisible;
    private View mKeyguardClockView;
    private KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private final KeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
        public void onLockScreenMagazinePreViewVisibilityChanged(boolean z) {
            boolean unused = KeyguardVerticalMoveHelper.this.mIsLockScreenMagazinePreViewVisible = z;
        }
    };
    private int mKeyguardVerticalGestureSlop;
    private LockScreenMagazinePreView mLockScreenMagazinePreView;
    private View mNotificationStackScroller;
    private NotificationPanelView mPanelView;
    private int mTouchSlop;
    private boolean mTracking;
    private float mTranslationPer;
    private VelocityTracker mVelocityTracker;

    KeyguardVerticalMoveHelper(Context context, NotificationPanelView notificationPanelView, View view, View view2, MiuiKeyguardFaceUnlockView miuiKeyguardFaceUnlockView, LockScreenMagazinePreView lockScreenMagazinePreView) {
        this.mContext = context;
        this.mKeyguardUpdateMonitor = KeyguardUpdateMonitor.getInstance(this.mContext);
        this.mKeyguardUpdateMonitor.registerCallback(this.mKeyguardUpdateMonitorCallback);
        this.mKeyguardClockView = view;
        this.mNotificationStackScroller = view2;
        this.mPanelView = notificationPanelView;
        this.mFaceUnlockView = miuiKeyguardFaceUnlockView;
        this.mLockScreenMagazinePreView = lockScreenMagazinePreView;
        initDimens();
    }

    private void initDimens() {
        this.mTouchSlop = ViewConfiguration.get(this.mContext).getScaledPagingTouchSlop();
        this.mKeyguardVerticalGestureSlop = this.mContext.getResources().getDimensionPixelSize(R.dimen.keyguard_vertical_gesture_slop);
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        float y = motionEvent.getY();
        float x = motionEvent.getX();
        if ((actionMasked != 0 && !this.mTracking) || this.mIsLockScreenMagazinePreViewVisible) {
            return false;
        }
        if (actionMasked != 0) {
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    trackMovement(motionEvent);
                    float f = this.mInitialTouchY - y;
                    if (f < 0.0f) {
                        f = 0.0f;
                    }
                    if (this.mKeyguardUpdateMonitor.isKeyguardShowing()) {
                        float min = Math.min(f / ((float) (this.mPanelView.getHeight() / 2)), 1.0f);
                        this.mTranslationPer = min;
                        doPanelViewAnimation();
                        if (KeyguardWallpaperUtils.isWallpaperShouldBlur(this.mContext)) {
                            ((MiuiKeyguardWallpaperController) Dependency.get(MiuiKeyguardWallpaperController.class)).updateKeyguardRatio(min - 4.0f, 0);
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
            this.mTracking = false;
            endMotionEvent(motionEvent, x, y, false);
        } else {
            this.mInitialTouchX = x;
            this.mInitialTouchY = y;
            this.mTracking = true;
            this.mInitialDownTime = SystemClock.uptimeMillis();
            initVelocityTracker();
            trackMovement(motionEvent);
        }
        return true;
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
        if (this.mPanelView.flingExpands(f4, f3, f, f2) || motionEvent.getActionMasked() == 3 || z) {
            this.mFaceUnlockView.updateFaceUnlockView();
            showOrHideKeyguard(true);
            if (this.mKeyguardUpdateMonitor.isKeyguardShowing()) {
                ((MiuiKeyguardWallpaperController) Dependency.get(MiuiKeyguardWallpaperController.class)).updateKeyguardRatio(-1.0f, KeyguardWallpaperUtils.isSupportWallpaperBlur() ? 300 : 0);
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
        this.mAnimator = ValueAnimator.ofFloat(new float[]{this.mTranslationPer, z ? 0.0f : 1.0f});
        this.mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                KeyguardVerticalMoveHelper.this.lambda$showOrHideKeyguard$0$KeyguardVerticalMoveHelper(valueAnimator);
            }
        });
        this.mAnimator.setDuration(300);
        this.mAnimator.setInterpolator(new DecelerateInterpolator());
        this.mAnimator.start();
    }

    public /* synthetic */ void lambda$showOrHideKeyguard$0$KeyguardVerticalMoveHelper(ValueAnimator valueAnimator) {
        this.mTranslationPer = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        doPanelViewAnimation();
    }

    private void doPanelViewAnimation() {
        float f = this.mTranslationPer;
        float f2 = 1.0f - ((1.0f - f) * (1.0f - f));
        if (!this.mPanelView.isForceBlack()) {
            float f3 = 1.0f - (0.1f * f2);
            this.mPanelView.setScaleX(f3);
            this.mPanelView.setScaleY(f3);
        }
        this.mPanelView.setTransitionAlpha(1.0f - f2);
    }

    private void trackMovement(MotionEvent motionEvent) {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.addMovement(motionEvent);
        }
    }

    private void initVelocityTracker() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.recycle();
        }
        this.mVelocityTracker = VelocityTracker.obtain();
    }
}
