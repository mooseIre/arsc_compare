package com.android.keyguard;

import android.animation.ValueAnimator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.animation.DecelerateInterpolator;
import com.android.keyguard.injector.KeyguardPanelViewInjector;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController;

public class BaseKeyguardMoveHelper {
    protected ValueAnimator mAnimator;
    protected float mInitialTouchX;
    protected float mInitialTouchY;
    protected MiuiNotificationPanelViewController mNotificationPanelViewController;
    protected float mTranslationPer;
    protected VelocityTracker mVelocityTracker;
    protected float x;
    protected float y;

    public BaseKeyguardMoveHelper() {
    }

    public BaseKeyguardMoveHelper(MiuiNotificationPanelViewController miuiNotificationPanelViewController) {
        this.mNotificationPanelViewController = miuiNotificationPanelViewController;
    }

    /* access modifiers changed from: protected */
    public void initCommonTouchEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        this.y = motionEvent.getY();
        float x2 = motionEvent.getX();
        this.x = x2;
        if (actionMasked == 0) {
            this.mInitialTouchX = x2;
            this.mInitialTouchY = this.y;
            initVelocityTracker();
            trackMovement(motionEvent);
        } else if (actionMasked == 1 || actionMasked == 2 || actionMasked == 3) {
            trackMovement(motionEvent);
        }
    }

    /* access modifiers changed from: protected */
    public void showOrHideKeyguard(boolean z) {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(this.mTranslationPer, z ? 0.0f : 1.0f);
        this.mAnimator = ofFloat;
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.keyguard.$$Lambda$BaseKeyguardMoveHelper$uqo2veyLYcmF1ghqEIFbRjTFz5s */

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                BaseKeyguardMoveHelper.this.lambda$showOrHideKeyguard$0$BaseKeyguardMoveHelper(valueAnimator);
            }
        });
        this.mAnimator.setDuration(300L);
        this.mAnimator.setInterpolator(new DecelerateInterpolator());
        this.mAnimator.start();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showOrHideKeyguard$0 */
    public /* synthetic */ void lambda$showOrHideKeyguard$0$BaseKeyguardMoveHelper(ValueAnimator valueAnimator) {
        this.mTranslationPer = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        doPanelViewAnimation();
    }

    /* access modifiers changed from: protected */
    public void doPanelViewAnimation() {
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
        Log.e("BaseKeyguardMoveHelper", " mTranslationPer:" + this.mTranslationPer);
        return 1.0f;
    }

    /* access modifiers changed from: protected */
    public void initVelocityTracker() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.recycle();
        }
        this.mVelocityTracker = VelocityTracker.obtain();
    }

    /* access modifiers changed from: protected */
    public void trackMovement(MotionEvent motionEvent) {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.addMovement(motionEvent);
        }
    }
}
