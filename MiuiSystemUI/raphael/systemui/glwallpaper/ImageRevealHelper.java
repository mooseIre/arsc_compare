package com.android.systemui.glwallpaper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import miui.view.animation.CubicEaseOutInterpolator;

public class ImageRevealHelper {
    private static final Interpolator CUBIC_EASE_OUT = new CubicEaseOutInterpolator();
    private static final Interpolator FAST_OUT_SLOW_IN = new PathInterpolator(0.4f, 0.0f, 0.2f, 1.0f);
    private final ValueAnimator mAnimator;
    private boolean mAwake = false;
    private float mReveal = 0.0f;
    /* access modifiers changed from: private */
    public final RevealStateListener mRevealListener;

    public interface RevealStateListener {
        void onRevealEnd();

        void onRevealStart();

        void onRevealStateChanged();
    }

    public ImageRevealHelper(RevealStateListener revealStateListener) {
        this.mRevealListener = revealStateListener;
        this.mAnimator = ValueAnimator.ofFloat(new float[0]);
        this.mAnimator.setInterpolator(CUBIC_EASE_OUT);
        this.mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                ImageRevealHelper.this.lambda$new$0$ImageRevealHelper(valueAnimator);
            }
        });
        this.mAnimator.addListener(new AnimatorListenerAdapter() {
            private boolean mIsCanceled;

            public void onAnimationCancel(Animator animator) {
                this.mIsCanceled = true;
            }

            public void onAnimationEnd(Animator animator) {
                if (!this.mIsCanceled && ImageRevealHelper.this.mRevealListener != null) {
                    ImageRevealHelper.this.mRevealListener.onRevealEnd();
                }
                this.mIsCanceled = false;
            }

            public void onAnimationStart(Animator animator) {
                if (ImageRevealHelper.this.mRevealListener != null) {
                    ImageRevealHelper.this.mRevealListener.onRevealStart();
                }
            }
        });
    }

    public /* synthetic */ void lambda$new$0$ImageRevealHelper(ValueAnimator valueAnimator) {
        this.mReveal = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        RevealStateListener revealStateListener = this.mRevealListener;
        if (revealStateListener != null) {
            revealStateListener.onRevealStateChanged();
        }
    }

    private void animate() {
        this.mAnimator.cancel();
        ValueAnimator valueAnimator = this.mAnimator;
        float[] fArr = new float[2];
        fArr[0] = this.mReveal;
        fArr[1] = !this.mAwake ? 1.0f : 0.0f;
        valueAnimator.setFloatValues(fArr);
        this.mAnimator.start();
    }

    public void cancelAnimate() {
        if (this.mAnimator.isRunning()) {
            this.mAnimator.cancel();
        }
    }

    public float getReveal() {
        return this.mReveal;
    }

    public void updateAwake(boolean z, long j) {
        this.mAwake = z;
        this.mAnimator.setDuration(j);
        if (this.mAwake || j != 0) {
            animate();
            return;
        }
        this.mReveal = 1.0f;
        this.mRevealListener.onRevealStart();
        this.mRevealListener.onRevealStateChanged();
        this.mRevealListener.onRevealEnd();
    }

    public void startUnlockAnim(boolean z, long j) {
        this.mAnimator.cancel();
        ValueAnimator valueAnimator = this.mAnimator;
        float[] fArr = new float[2];
        fArr[0] = z ? 0.0f : 1.0f;
        fArr[1] = 0.0f;
        valueAnimator.setFloatValues(fArr);
        this.mAnimator.setDuration(j);
        this.mAnimator.start();
    }
}
