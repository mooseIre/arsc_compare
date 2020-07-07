package com.android.systemui.qs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.view.ViewAnimationUtils;

public class QSDetailClipper {
    /* access modifiers changed from: private */
    public Animator mAnimator;
    /* access modifiers changed from: private */
    public final View mDetail;
    private final AnimatorListenerAdapter mGoneOnEnd = new AnimatorListenerAdapter() {
        public void onAnimationEnd(Animator animator) {
            QSDetailClipper.this.mDetail.setVisibility(8);
            Animator unused = QSDetailClipper.this.mAnimator = null;
        }
    };
    private final AnimatorListenerAdapter mVisibleOnStart = new AnimatorListenerAdapter() {
        public void onAnimationStart(Animator animator) {
            QSDetailClipper.this.mDetail.setVisibility(0);
        }

        public void onAnimationEnd(Animator animator) {
            Animator unused = QSDetailClipper.this.mAnimator = null;
        }
    };

    public QSDetailClipper(View view) {
        this.mDetail = view;
    }

    public void animateCircularClip(int i, int i2, boolean z, Animator.AnimatorListener animatorListener) {
        Animator animator = this.mAnimator;
        if (animator != null) {
            animator.cancel();
            this.mAnimator.removeAllListeners();
        }
        int width = this.mDetail.getWidth() - i;
        int height = this.mDetail.getHeight() - i2;
        int i3 = 0;
        if (i < 0 || width < 0 || i2 < 0 || height < 0) {
            i3 = Math.min(Math.min(Math.min(Math.abs(i), Math.abs(i2)), Math.abs(width)), Math.abs(height));
        }
        int i4 = i * i;
        int i5 = i2 * i2;
        int i6 = width * width;
        int i7 = height * height;
        int max = (int) Math.max((double) ((int) Math.max((double) ((int) Math.max((double) ((int) Math.ceil(Math.sqrt((double) (i4 + i5)))), Math.ceil(Math.sqrt((double) (i5 + i6))))), Math.ceil(Math.sqrt((double) (i6 + i7))))), Math.ceil(Math.sqrt((double) (i4 + i7))));
        if (z) {
            this.mAnimator = ViewAnimationUtils.createCircularReveal(this.mDetail, i, i2, (float) i3, (float) max);
        } else {
            this.mAnimator = ViewAnimationUtils.createCircularReveal(this.mDetail, i, i2, (float) max, (float) i3);
        }
        this.mAnimator.setDuration(420);
        this.mAnimator.setInterpolator(QSAnimation.INTERPOLATOR);
        if (animatorListener != null) {
            this.mAnimator.addListener(animatorListener);
        }
        if (z) {
            this.mAnimator.addListener(this.mVisibleOnStart);
        } else {
            this.mAnimator.addListener(this.mGoneOnEnd);
        }
        this.mAnimator.start();
    }
}
