package com.android.systemui.pip;

import android.animation.AnimationHandler;
import android.animation.Animator;
import android.animation.RectEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.view.SurfaceControl;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.graphics.SfVsyncFrameCallbackProvider;
import com.android.systemui.Interpolators;
import com.android.systemui.pip.PipSurfaceTransactionHelper;

public class PipAnimationController {
    private PipTransitionAnimator mCurrentAnimator;
    private ThreadLocal<AnimationHandler> mSfAnimationHandlerThreadLocal = ThreadLocal.withInitial($$Lambda$PipAnimationController$iXb7MLu8McpFbUwX5eyjXMVFMI.INSTANCE);
    private final PipSurfaceTransactionHelper mSurfaceTransactionHelper;

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

    static /* synthetic */ AnimationHandler lambda$new$0() {
        AnimationHandler animationHandler = new AnimationHandler();
        animationHandler.setProvider(new SfVsyncFrameCallbackProvider());
        return animationHandler;
    }

    PipAnimationController(Context context, PipSurfaceTransactionHelper pipSurfaceTransactionHelper) {
        this.mSurfaceTransactionHelper = pipSurfaceTransactionHelper;
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
    public PipTransitionAnimator getAnimator(SurfaceControl surfaceControl, Rect rect, Rect rect2, Rect rect3) {
        PipTransitionAnimator pipTransitionAnimator = this.mCurrentAnimator;
        if (pipTransitionAnimator == null) {
            PipTransitionAnimator<Rect> ofBounds = PipTransitionAnimator.ofBounds(surfaceControl, rect, rect2, rect3);
            setupPipTransitionAnimator(ofBounds);
            this.mCurrentAnimator = ofBounds;
        } else if (pipTransitionAnimator.getAnimationType() == 1 && this.mCurrentAnimator.isRunning()) {
            this.mCurrentAnimator.setDestinationBounds(rect2);
        } else if (this.mCurrentAnimator.getAnimationType() != 0 || !this.mCurrentAnimator.isRunning()) {
            this.mCurrentAnimator.cancel();
            PipTransitionAnimator<Rect> ofBounds2 = PipTransitionAnimator.ofBounds(surfaceControl, rect, rect2, rect3);
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
        pipTransitionAnimator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        pipTransitionAnimator.setFloatValues(0.0f, 1.0f);
        pipTransitionAnimator.setAnimationHandler(this.mSfAnimationHandlerThreadLocal.get());
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
        public void onStartTransaction(SurfaceControl surfaceControl, SurfaceControl.Transaction transaction) {
        }

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
        @VisibleForTesting
        public void setSurfaceControlTransactionFactory(PipSurfaceTransactionHelper.SurfaceControlTransactionFactory surfaceControlTransactionFactory) {
            this.mSurfaceControlTransactionFactory = surfaceControlTransactionFactory;
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
                /* class com.android.systemui.pip.PipAnimationController.PipTransitionAnimator.AnonymousClass1 */

                /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.systemui.pip.PipAnimationController$PipTransitionAnimator$1 */
                /* JADX WARN: Multi-variable type inference failed */
                /* access modifiers changed from: package-private */
                @Override // com.android.systemui.pip.PipAnimationController.PipTransitionAnimator
                public void applySurfaceControlTransaction(SurfaceControl surfaceControl, SurfaceControl.Transaction transaction, float f) {
                    float floatValue = (((Float) getStartValue()).floatValue() * (1.0f - f)) + (((Float) getEndValue()).floatValue() * f);
                    setCurrentValue(Float.valueOf(floatValue));
                    getSurfaceTransactionHelper().alpha(transaction, surfaceControl, floatValue);
                    transaction.apply();
                }

                /* access modifiers changed from: package-private */
                @Override // com.android.systemui.pip.PipAnimationController.PipTransitionAnimator
                public void onStartTransaction(SurfaceControl surfaceControl, SurfaceControl.Transaction transaction) {
                    PipSurfaceTransactionHelper surfaceTransactionHelper = getSurfaceTransactionHelper();
                    surfaceTransactionHelper.resetScale(transaction, surfaceControl, getDestinationBounds());
                    surfaceTransactionHelper.crop(transaction, surfaceControl, getDestinationBounds());
                    surfaceTransactionHelper.round(transaction, surfaceControl, shouldApplyCornerRadius());
                    transaction.show(surfaceControl);
                    transaction.apply();
                }

                /* access modifiers changed from: package-private */
                public void updateEndValue(Float f) {
                    super.updateEndValue((Object) f);
                    this.mStartValue = this.mCurrentValue;
                }
            };
        }

        static PipTransitionAnimator<Rect> ofBounds(SurfaceControl surfaceControl, Rect rect, Rect rect2, Rect rect3) {
            final Rect rect4 = new Rect(rect);
            final Rect rect5 = rect3 != null ? new Rect(rect3.left - rect.left, rect3.top - rect.top, rect.right - rect3.right, rect.bottom - rect3.bottom) : null;
            final Rect rect6 = new Rect(0, 0, 0, 0);
            return new PipTransitionAnimator<Rect>(surfaceControl, 0, rect2, new Rect(rect), new Rect(rect2)) {
                /* class com.android.systemui.pip.PipAnimationController.PipTransitionAnimator.AnonymousClass2 */
                private final RectEvaluator mInsetsEvaluator = new RectEvaluator(new Rect());
                private final RectEvaluator mRectEvaluator = new RectEvaluator(new Rect());

                /* JADX DEBUG: Multi-variable search result rejected for r9v0, resolved type: com.android.systemui.pip.PipAnimationController$PipTransitionAnimator$2 */
                /* JADX WARN: Multi-variable type inference failed */
                /* access modifiers changed from: package-private */
                @Override // com.android.systemui.pip.PipAnimationController.PipTransitionAnimator
                public void applySurfaceControlTransaction(SurfaceControl surfaceControl, SurfaceControl.Transaction transaction, float f) {
                    Rect rect = (Rect) getStartValue();
                    Rect rect2 = (Rect) getEndValue();
                    Rect evaluate = this.mRectEvaluator.evaluate(f, rect, rect2);
                    setCurrentValue(evaluate);
                    if (!inScaleTransition()) {
                        Rect rect3 = rect5;
                        if (rect3 != null) {
                            getSurfaceTransactionHelper().scaleAndCrop(transaction, surfaceControl, rect4, evaluate, this.mInsetsEvaluator.evaluate(f, rect6, rect3));
                        } else {
                            getSurfaceTransactionHelper().scale(transaction, surfaceControl, rect, evaluate);
                        }
                    } else if (PipAnimationController.isOutPipDirection(getTransitionDirection())) {
                        getSurfaceTransactionHelper().scale(transaction, surfaceControl, rect2, evaluate);
                    } else {
                        getSurfaceTransactionHelper().scale(transaction, surfaceControl, rect, evaluate);
                    }
                    transaction.apply();
                }

                /* access modifiers changed from: package-private */
                @Override // com.android.systemui.pip.PipAnimationController.PipTransitionAnimator
                public void onStartTransaction(SurfaceControl surfaceControl, SurfaceControl.Transaction transaction) {
                    PipSurfaceTransactionHelper surfaceTransactionHelper = getSurfaceTransactionHelper();
                    surfaceTransactionHelper.alpha(transaction, surfaceControl, 1.0f);
                    surfaceTransactionHelper.round(transaction, surfaceControl, shouldApplyCornerRadius());
                    transaction.show(surfaceControl);
                    transaction.apply();
                }

                /* access modifiers changed from: package-private */
                @Override // com.android.systemui.pip.PipAnimationController.PipTransitionAnimator
                public void onEndTransaction(SurfaceControl surfaceControl, SurfaceControl.Transaction transaction) {
                    PipSurfaceTransactionHelper surfaceTransactionHelper = getSurfaceTransactionHelper();
                    surfaceTransactionHelper.resetScale(transaction, surfaceControl, getDestinationBounds());
                    surfaceTransactionHelper.crop(transaction, surfaceControl, getDestinationBounds());
                }

                /* access modifiers changed from: package-private */
                public void updateEndValue(Rect rect) {
                    T t;
                    super.updateEndValue((Object) rect);
                    T t2 = this.mStartValue;
                    if (t2 != null && (t = this.mCurrentValue) != null) {
                        t2.set(t);
                    }
                }
            };
        }
    }
}
