package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import com.android.keyguard.AlphaOptimizedImageButton;
import com.android.systemui.Interpolators;

public class SettingsButton extends AlphaOptimizedImageButton {
    private ObjectAnimator mAnimator;
    private final Runnable mLongPressCallback = new Runnable() {
        /* class com.android.systemui.statusbar.phone.SettingsButton.AnonymousClass3 */

        public void run() {
            SettingsButton.this.startAccelSpin();
        }
    };
    private float mSlop = ((float) ViewConfiguration.get(getContext()).getScaledTouchSlop());
    private boolean mUpToSpeed;

    public SettingsButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public boolean isTunerClick() {
        return this.mUpToSpeed;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked != 1) {
            if (actionMasked == 2) {
                float x = motionEvent.getX();
                float y = motionEvent.getY();
                float f = this.mSlop;
                if (x < (-f) || y < (-f) || x > ((float) getWidth()) + this.mSlop || y > ((float) getHeight()) + this.mSlop) {
                    cancelLongClick();
                }
            } else if (actionMasked == 3) {
                cancelLongClick();
            }
        } else if (this.mUpToSpeed) {
            startExitAnimation();
        } else {
            cancelLongClick();
        }
        return super.onTouchEvent(motionEvent);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cancelLongClick() {
        cancelAnimation();
        this.mUpToSpeed = false;
        removeCallbacks(this.mLongPressCallback);
    }

    private void cancelAnimation() {
        ObjectAnimator objectAnimator = this.mAnimator;
        if (objectAnimator != null) {
            objectAnimator.removeAllListeners();
            this.mAnimator.cancel();
            this.mAnimator = null;
        }
    }

    private void startExitAnimation() {
        animate().translationX(((float) ((View) getParent().getParent()).getWidth()) - getX()).alpha(0.0f).setDuration(350).setInterpolator(AnimationUtils.loadInterpolator(((ImageButton) this).mContext, 17563650)).setListener(new Animator.AnimatorListener() {
            /* class com.android.systemui.statusbar.phone.SettingsButton.AnonymousClass1 */

            public void onAnimationCancel(Animator animator) {
            }

            public void onAnimationRepeat(Animator animator) {
            }

            public void onAnimationStart(Animator animator) {
            }

            public void onAnimationEnd(Animator animator) {
                SettingsButton.this.setAlpha(1.0f);
                SettingsButton.this.setTranslationX(0.0f);
                SettingsButton.this.cancelLongClick();
            }
        }).start();
    }

    /* access modifiers changed from: protected */
    public void startAccelSpin() {
        cancelAnimation();
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this, View.ROTATION, 0.0f, 360.0f);
        this.mAnimator = ofFloat;
        ofFloat.setInterpolator(AnimationUtils.loadInterpolator(((ImageButton) this).mContext, 17563648));
        this.mAnimator.setDuration(750L);
        this.mAnimator.addListener(new Animator.AnimatorListener() {
            /* class com.android.systemui.statusbar.phone.SettingsButton.AnonymousClass2 */

            public void onAnimationCancel(Animator animator) {
            }

            public void onAnimationRepeat(Animator animator) {
            }

            public void onAnimationStart(Animator animator) {
            }

            public void onAnimationEnd(Animator animator) {
                SettingsButton.this.startContinuousSpin();
            }
        });
        this.mAnimator.start();
    }

    /* access modifiers changed from: protected */
    public void startContinuousSpin() {
        cancelAnimation();
        performHapticFeedback(0);
        this.mUpToSpeed = true;
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this, View.ROTATION, 0.0f, 360.0f);
        this.mAnimator = ofFloat;
        ofFloat.setInterpolator(Interpolators.LINEAR);
        this.mAnimator.setDuration(375L);
        this.mAnimator.setRepeatCount(-1);
        this.mAnimator.start();
    }
}
