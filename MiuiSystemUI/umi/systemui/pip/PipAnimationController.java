package com.android.systemui.pip;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.view.SurfaceControl;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import com.android.systemui.pip.PipSurfaceTransactionHelper;

public class PipAnimationController {
    private PipTransitionAnimator mCurrentAnimator;
    private final Interpolator mFastOutSlowInInterpolator;
    private PipSurfaceTransactionHelper mSurfaceTransactionHelper;

    public static class PipAnimationCallback {
        public abstract void onPipAnimationCancel(PipTransitionAnimator pipTransitionAnimator);

        public abstract void onPipAnimationEnd(SurfaceControl.Transaction transaction, PipTransitionAnimator pipTransitionAnimator);

        public abstract void onPipAnimationStart(PipTransitionAnimator pipTransitionAnimator);
    }

    public static boolean isInPipDirection(int i) {
        return i == 2;
    }

    public static boolean isOutPipDirection(int i) {
        return i == 3 || i == 4;
    }

    public PipAnimationController(Context context) {
        this.mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(context, 17563661);
        this.mSurfaceTransactionHelper = PipSurfaceTransactionHelper.getInstance(context);
    }

    /* access modifiers changed from: package-private */
    public PipTransitionAnimator getAnimator(SurfaceControl surfaceControl, Rect rect, float f, float f2) {
        PipTransitionAnimator pipTransitionAnimator = this.mCurrentAnimator;
        if (pipTransitionAnimator == null) {
            PipTransitionAnimator<Float> ofAlpha = PipTransitionAnimator.ofAlpha(surfaceControl, rect, f, f2);
            setupPipTransitionAnimator(ofAlpha);
            this.mCurrentAnimator = ofAlpha;
        } else if (pipTransitionAnimator.getAnimationType() != 1 || !this.mCurrentAnimator.isRunning()) {
            this.mCurrentAnimator.cancel();
            PipTransitionAnimator<Float> ofAlpha2 = PipTransitionAnimator.ofAlpha(surfaceControl, rect, f, f2);
            setupPipTransitionAnimator(ofAlpha2);
            this.mCurrentAnimator = ofAlpha2;
        } else {
            this.mCurrentAnimator.updateEndValue(Float.valueOf(f2));
        }
        return this.mCurrentAnimator;
    }

    /* access modifiers changed from: package-private */
    public PipTransitionAnimator getAnimator(SurfaceControl surfaceControl, Rect rect, Rect rect2) {
        PipTransitionAnimator pipTransitionAnimator = this.mCurrentAnimator;
        if (pipTransitionAnimator == null) {
            PipTransitionAnimator<Rect> ofBounds = PipTransitionAnimator.ofBounds(surfaceControl, rect, rect2);
            setupPipTransitionAnimator(ofBounds);
            this.mCurrentAnimator = ofBounds;
        } else if (pipTransitionAnimator.getAnimationType() == 1 && this.mCurrentAnimator.isRunning()) {
            this.mCurrentAnimator.setDestinationBounds(rect2);
        } else if (this.mCurrentAnimator.getAnimationType() != 0 || !this.mCurrentAnimator.isRunning()) {
            this.mCurrentAnimator.cancel();
            PipTransitionAnimator<Rect> ofBounds2 = PipTransitionAnimator.ofBounds(surfaceControl, rect, rect2);
            setupPipTransitionAnimator(ofBounds2);
            this.mCurrentAnimator = ofBounds2;
        } else {
            this.mCurrentAnimator.setDestinationBounds(rect2);
            this.mCurrentAnimator.updateEndValue(new Rect(rect2));
        }
        return this.mCurrentAnimator;
    }

    /* access modifiers changed from: package-private */
    public PipTransitionAnimator getCurrentAnimator() {
        return this.mCurrentAnimator;
    }

    private PipTransitionAnimator setupPipTransitionAnimator(PipTransitionAnimator pipTransitionAnimator) {
        pipTransitionAnimator.setSurfaceTransactionHelper(this.mSurfaceTransactionHelper);
        pipTransitionAnimator.setInterpolator(this.mFastOutSlowInInterpolator);
        pipTransitionAnimator.setFloatValues(new float[]{0.0f, 1.0f});
        return pipTransitionAnimator;
    }

    public static abstract class PipTransitionAnimator<T> extends ValueAnimator implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {
        private final int mAnimationType;
        protected T mCurrentValue;
        private final Rect mDestinationBounds;
        private T mEndValue;
        private final SurfaceControl mLeash;
        private PipAnimationCallback mPipAnimationCallback;
        protected T mStartValue;
        private PipSurfaceTransactionHelper.SurfaceControlTransactionFactory mSurfaceControlTransactionFactory;
        private PipSurfaceTransactionHelper mSurfaceTransactionHelper;
        private int mTransitionDirection;

        /* access modifiers changed from: package-private */
        public abstract void applySurfaceControlTransaction(SurfaceControl surfaceControl, SurfaceControl.Transaction transaction, float f);

        public void onAnimationRepeat(Animator animator) {
        }

        /* access modifiers changed from: package-private */
        public void onEndTransaction(SurfaceControl surfaceControl, SurfaceControl.Transaction transaction) {
        }

        /* access modifiers changed from: package-private */
        public abstract void onStartTransaction(SurfaceControl surfaceControl, SurfaceControl.Transaction transaction);

        private PipTransitionAnimator(SurfaceControl surfaceControl, int i, Rect rect, T t, T t2) {
            Rect rect2 = new Rect();
            this.mDestinationBounds = rect2;
            this.mLeash = surfaceControl;
            this.mAnimationType = i;
            rect2.set(rect);
            this.mStartValue = t;
            this.mEndValue = t2;
            addListener(this);
            addUpdateListener(this);
            this.mSurfaceControlTransactionFactory = $$Lambda$0FLZQAxNoOm85ohJ3bgjkYQDWsU.INSTANCE;
            this.mTransitionDirection = 0;
        }

        public void onAnimationStart(Animator animator) {
            this.mCurrentValue = this.mStartValue;
            onStartTransaction(this.mLeash, newSurfaceControlTransaction());
            PipAnimationCallback pipAnimationCallback = this.mPipAnimationCallback;
            if (pipAnimationCallback != null) {
                pipAnimationCallback.onPipAnimationStart(this);
            }
        }

        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            applySurfaceControlTransaction(this.mLeash, newSurfaceControlTransaction(), valueAnimator.getAnimatedFraction());
        }

        public void onAnimationEnd(Animator animator) {
            this.mCurrentValue = this.mEndValue;
            SurfaceControl.Transaction newSurfaceControlTransaction = newSurfaceControlTransaction();
            onEndTransaction(this.mLeash, newSurfaceControlTransaction);
            PipAnimationCallback pipAnimationCallback = this.mPipAnimationCallback;
            if (pipAnimationCallback != null) {
                pipAnimationCallback.onPipAnimationEnd(newSurfaceControlTransaction, this);
            }
        }

        public void onAnimationCancel(Animator animator) {
            PipAnimationCallback pipAnimationCallback = this.mPipAnimationCallback;
            if (pipAnimationCallback != null) {
                pipAnimationCallback.onPipAnimationCancel(this);
            }
        }

        /* access modifiers changed from: package-private */
        public int getAnimationType() {
            return this.mAnimationType;
        }

        /* access modifiers changed from: package-private */
        public PipTransitionAnimator<T> setPipAnimationCallback(PipAnimationCallback pipAnimationCallback) {
            this.mPipAnimationCallback = pipAnimationCallback;
            return this;
        }

        /* access modifiers changed from: package-private */
        public int getTransitionDirection() {
            return this.mTransitionDirection;
        }

        /* access modifiers changed from: package-private */
        public PipTransitionAnimator<T> setTransitionDirection(int i) {
            if (i != 1) {
                this.mTransitionDirection = i;
            }
            return this;
        }

        /* access modifiers changed from: package-private */
        public T getStartValue() {
            return this.mStartValue;
        }

        /* access modifiers changed from: package-private */
        public T getEndValue() {
            return this.mEndValue;
        }

        /* access modifiers changed from: package-private */
        public Rect getDestinationBounds() {
            return this.mDestinationBounds;
        }

        /* access modifiers changed from: package-private */
        public void setDestinationBounds(Rect rect) {
            this.mDestinationBounds.set(rect);
            if (this.mAnimationType == 1) {
                onStartTransaction(this.mLeash, newSurfaceControlTransaction());
            }
        }

        /* access modifiers changed from: package-private */
        public void setCurrentValue(T t) {
            this.mCurrentValue = t;
        }

        /* access modifiers changed from: package-private */
        public boolean shouldApplyCornerRadius() {
            return !PipAnimationController.isOutPipDirection(this.mTransitionDirection);
        }

        /* access modifiers changed from: package-private */
        public boolean inScaleTransition() {
            if (this.mAnimationType != 0) {
                return false;
            }
            return !PipAnimationController.isInPipDirection(getTransitionDirection());
        }

        /* access modifiers changed from: package-private */
        public void updateEndValue(T t) {
            this.mEndValue = t;
        }

        /* access modifiers changed from: package-private */
        public SurfaceControl.Transaction newSurfaceControlTransaction() {
            return this.mSurfaceControlTransactionFactory.getTransaction();
        }

        /* access modifiers changed from: package-private */
        public PipSurfaceTransactionHelper getSurfaceTransactionHelper() {
            return this.mSurfaceTransactionHelper;
        }

        /* access modifiers changed from: package-private */
        public void setSurfaceTransactionHelper(PipSurfaceTransactionHelper pipSurfaceTransactionHelper) {
            this.mSurfaceTransactionHelper = pipSurfaceTransactionHelper;
        }

        static PipTransitionAnimator<Float> ofAlpha(SurfaceControl surfaceControl, Rect rect, float f, float f2) {
            return new PipTransitionAnimator<Float>(surfaceControl, 1, rect, Float.valueOf(f), Float.valueOf(f2)) {
                /* access modifiers changed from: package-private */
                public void applySurfaceControlTransaction(SurfaceControl surfaceControl, SurfaceControl.Transaction transaction, float f) {
                    float floatValue = (((Float) getStartValue()).floatValue() * (1.0f - f)) + (((Float) getEndValue()).floatValue() * f);
                    setCurrentValue(Float.valueOf(floatValue));
                    getSurfaceTransactionHelper().alpha(transaction, surfaceControl, floatValue);
                    transaction.apply();
                }

                /* access modifiers changed from: package-private */
                public void onStartTransaction(SurfaceControl surfaceControl, SurfaceControl.Transaction transaction) {
                    PipSurfaceTransactionHelper surfaceTransactionHelper = getSurfaceTransactionHelper();
                    surfaceTransactionHelper.crop(transaction, surfaceControl, getDestinationBounds());
                    surfaceTransactionHelper.round(transaction, surfaceControl, shouldApplyCornerRadius());
                    transaction.show(surfaceControl);
                    transaction.apply();
                }

                /* access modifiers changed from: package-private */
                public void updateEndValue(Float f) {
                    super.updateEndValue(f);
                    this.mStartValue = this.mCurrentValue;
                }
            };
        }

        static PipTransitionAnimator<Rect> ofBounds(SurfaceControl surfaceControl, Rect rect, Rect rect2) {
            return new PipTransitionAnimator<Rect>(surfaceControl, 0, rect2, new Rect(rect), new Rect(rect2)) {
                private final Rect mTmpRect = new Rect();

                private int getCastedFractionValue(float f, float f2, float f3) {
                    return (int) ((f * (1.0f - f3)) + (f2 * f3) + 0.5f);
                }

                /* access modifiers changed from: package-private */
                public void applySurfaceControlTransaction(SurfaceControl surfaceControl, SurfaceControl.Transaction transaction, float f) {
                    Rect rect = (Rect) getStartValue();
                    Rect rect2 = (Rect) getEndValue();
                    this.mTmpRect.set(getCastedFractionValue((float) rect.left, (float) rect2.left, f), getCastedFractionValue((float) rect.top, (float) rect2.top, f), getCastedFractionValue((float) rect.right, (float) rect2.right, f), getCastedFractionValue((float) rect.bottom, (float) rect2.bottom, f));
                    setCurrentValue(this.mTmpRect);
                    if (!inScaleTransition()) {
                        getSurfaceTransactionHelper().crop(transaction, surfaceControl, this.mTmpRect);
                    } else if (PipAnimationController.isOutPipDirection(getTransitionDirection())) {
                        getSurfaceTransactionHelper().scale(transaction, surfaceControl, rect2, this.mTmpRect);
                    } else {
                        getSurfaceTransactionHelper().scale(transaction, surfaceControl, rect, this.mTmpRect);
                    }
                    transaction.apply();
                }

                /* access modifiers changed from: package-private */
                public void onStartTransaction(SurfaceControl surfaceControl, SurfaceControl.Transaction transaction) {
                    PipSurfaceTransactionHelper surfaceTransactionHelper = getSurfaceTransactionHelper();
                    surfaceTransactionHelper.alpha(transaction, surfaceControl, 1.0f);
                    surfaceTransactionHelper.round(transaction, surfaceControl, shouldApplyCornerRadius());
                    transaction.show(surfaceControl);
                    transaction.apply();
                }

                /* access modifiers changed from: package-private */
                public void onEndTransaction(SurfaceControl surfaceControl, SurfaceControl.Transaction transaction) {
                    if (inScaleTransition()) {
                        PipSurfaceTransactionHelper surfaceTransactionHelper = getSurfaceTransactionHelper();
                        surfaceTransactionHelper.resetScale(transaction, surfaceControl, getDestinationBounds());
                        surfaceTransactionHelper.crop(transaction, surfaceControl, getDestinationBounds());
                    }
                }

                /* access modifiers changed from: package-private */
                public void updateEndValue(Rect rect) {
                    T t;
                    super.updateEndValue(rect);
                    T t2 = this.mStartValue;
                    if (t2 != null && (t = this.mCurrentValue) != null) {
                        ((Rect) t2).set((Rect) t);
                    }
                }
            };
        }
    }
}
