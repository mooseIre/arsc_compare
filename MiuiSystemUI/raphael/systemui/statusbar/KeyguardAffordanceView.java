package com.android.systemui.statusbar;

import android.R;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.CanvasProperty;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RecordingCanvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.Interpolators;

public class KeyguardAffordanceView extends ImageView {
    private ValueAnimator mAlphaAnimator;
    private AnimatorListenerAdapter mAlphaEndListener;
    private int mCenterX;
    private int mCenterY;
    private ValueAnimator mCircleAnimator;
    private int mCircleColor;
    private AnimatorListenerAdapter mCircleEndListener;
    private final Paint mCirclePaint;
    private float mCircleRadius;
    private float mCircleStartRadius;
    private float mCircleStartValue;
    private boolean mCircleWillBeHidden;
    private AnimatorListenerAdapter mClipEndListener;
    private final ArgbEvaluator mColorInterpolator;
    protected final int mDarkIconColor;
    private boolean mFinishing;
    private CanvasProperty<Float> mHwCenterX;
    private CanvasProperty<Float> mHwCenterY;
    private CanvasProperty<Paint> mHwCirclePaint;
    private CanvasProperty<Float> mHwCircleRadius;
    private float mImageScale;
    private boolean mLaunchingAffordance;
    private float mMaxCircleSize;
    private final int mMinBackgroundRadius;
    protected final int mNormalColor;
    private Animator mPreviewClipper;
    private View mPreviewView;
    private float mRestingAlpha;
    private ValueAnimator mScaleAnimator;
    private AnimatorListenerAdapter mScaleEndListener;
    private boolean mShouldTint;
    private boolean mSupportHardware;
    private int[] mTempPoint;

    public KeyguardAffordanceView(Context context) {
        this(context, null);
    }

    public KeyguardAffordanceView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public KeyguardAffordanceView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public KeyguardAffordanceView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mTempPoint = new int[2];
        this.mImageScale = 1.0f;
        this.mRestingAlpha = 1.0f;
        this.mShouldTint = true;
        this.mClipEndListener = new AnimatorListenerAdapter() {
            /* class com.android.systemui.statusbar.KeyguardAffordanceView.AnonymousClass1 */

            public void onAnimationEnd(Animator animator) {
                KeyguardAffordanceView.this.mPreviewClipper = null;
            }
        };
        this.mCircleEndListener = new AnimatorListenerAdapter() {
            /* class com.android.systemui.statusbar.KeyguardAffordanceView.AnonymousClass2 */

            public void onAnimationEnd(Animator animator) {
                KeyguardAffordanceView.this.mCircleAnimator = null;
            }
        };
        this.mScaleEndListener = new AnimatorListenerAdapter() {
            /* class com.android.systemui.statusbar.KeyguardAffordanceView.AnonymousClass3 */

            public void onAnimationEnd(Animator animator) {
                KeyguardAffordanceView.this.mScaleAnimator = null;
            }
        };
        this.mAlphaEndListener = new AnimatorListenerAdapter() {
            /* class com.android.systemui.statusbar.KeyguardAffordanceView.AnonymousClass4 */

            public void onAnimationEnd(Animator animator) {
                KeyguardAffordanceView.this.mAlphaAnimator = null;
            }
        };
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.ImageView);
        Paint paint = new Paint();
        this.mCirclePaint = paint;
        paint.setAntiAlias(true);
        this.mCircleColor = -1;
        this.mCirclePaint.setColor(-1);
        this.mNormalColor = obtainStyledAttributes.getColor(5, -1);
        this.mDarkIconColor = -16777216;
        this.mMinBackgroundRadius = ((ImageView) this).mContext.getResources().getDimensionPixelSize(C0012R$dimen.keyguard_affordance_min_background_radius);
        this.mColorInterpolator = new ArgbEvaluator();
        new FlingAnimationUtils(((ImageView) this).mContext.getResources().getDisplayMetrics(), 0.3f);
        obtainStyledAttributes.recycle();
    }

    public void setImageDrawable(Drawable drawable, boolean z) {
        super.setImageDrawable(drawable);
        this.mShouldTint = z;
        updateIconColor();
    }

    public boolean shouldTint() {
        return this.mShouldTint;
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        this.mCenterX = getWidth() / 2;
        this.mCenterY = getHeight() / 2;
        this.mMaxCircleSize = getMaxCircleSize();
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        this.mSupportHardware = canvas.isHardwareAccelerated();
        drawBackgroundCircle(canvas);
        canvas.save();
        float f = this.mImageScale;
        canvas.scale(f, f, (float) (getWidth() / 2), (float) (getHeight() / 2));
        super.onDraw(canvas);
        canvas.restore();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateIconColor() {
        if (this.mShouldTint) {
            getDrawable().mutate().setColorFilter(((Integer) this.mColorInterpolator.evaluate(Math.min(1.0f, this.mCircleRadius / ((float) this.mMinBackgroundRadius)), Integer.valueOf(this.mNormalColor), Integer.valueOf(this.mDarkIconColor))).intValue(), PorterDuff.Mode.SRC_ATOP);
        }
    }

    private void drawBackgroundCircle(Canvas canvas) {
        CanvasProperty<Float> canvasProperty;
        if (this.mCircleRadius <= 0.0f && !this.mFinishing) {
            return;
        }
        if (!this.mFinishing || !this.mSupportHardware || (canvasProperty = this.mHwCenterX) == null) {
            updateCircleColor();
            canvas.drawCircle((float) this.mCenterX, (float) this.mCenterY, this.mCircleRadius, this.mCirclePaint);
            return;
        }
        ((RecordingCanvas) canvas).drawCircle(canvasProperty, this.mHwCenterY, this.mHwCircleRadius, this.mHwCirclePaint);
    }

    private void updateCircleColor() {
        float f = this.mCircleRadius;
        int i = this.mMinBackgroundRadius;
        float max = (Math.max(0.0f, Math.min(1.0f, (f - ((float) i)) / (((float) i) * 0.5f))) * 0.5f) + 0.5f;
        View view = this.mPreviewView;
        if (view != null && view.getVisibility() == 0) {
            max *= 1.0f - (Math.max(0.0f, this.mCircleRadius - this.mCircleStartRadius) / (this.mMaxCircleSize - this.mCircleStartRadius));
        }
        this.mCirclePaint.setColor(Color.argb((int) (((float) Color.alpha(this.mCircleColor)) * max), Color.red(this.mCircleColor), Color.green(this.mCircleColor), Color.blue(this.mCircleColor)));
    }

    public void instantFinishAnimation() {
        cancelAnimator(this.mPreviewClipper);
        View view = this.mPreviewView;
        if (view != null) {
            view.setClipBounds(null);
            this.mPreviewView.setVisibility(0);
        }
        this.mCircleRadius = getMaxCircleSize();
        setImageAlpha(0.0f, false);
        invalidate();
    }

    private float getMaxCircleSize() {
        getLocationInWindow(this.mTempPoint);
        float f = (float) (this.mTempPoint[0] + this.mCenterX);
        return (float) Math.hypot((double) Math.max(((float) getRootView().getWidth()) - f, f), (double) ((float) (this.mTempPoint[1] + this.mCenterY)));
    }

    public void setCircleRadius(float f, boolean z) {
        setCircleRadius(f, z, false);
    }

    public void setCircleRadiusWithoutAnimation(float f) {
        cancelAnimator(this.mCircleAnimator);
        setCircleRadius(f, false, true);
    }

    private void setCircleRadius(float f, boolean z, boolean z2) {
        Interpolator interpolator;
        View view;
        boolean z3 = (this.mCircleAnimator != null && this.mCircleWillBeHidden) || (this.mCircleAnimator == null && this.mCircleRadius == 0.0f);
        int i = (f > 0.0f ? 1 : (f == 0.0f ? 0 : -1));
        boolean z4 = i == 0;
        if (!(z3 != z4 && !z2)) {
            ValueAnimator valueAnimator = this.mCircleAnimator;
            if (valueAnimator == null) {
                this.mCircleRadius = f;
                updateIconColor();
                invalidate();
                if (z4 && (view = this.mPreviewView) != null) {
                    view.setVisibility(4);
                }
            } else if (!this.mCircleWillBeHidden) {
                valueAnimator.getValues()[0].setFloatValues(this.mCircleStartValue + (f - ((float) this.mMinBackgroundRadius)), f);
                ValueAnimator valueAnimator2 = this.mCircleAnimator;
                valueAnimator2.setCurrentPlayTime(valueAnimator2.getCurrentPlayTime());
            }
        } else {
            cancelAnimator(this.mCircleAnimator);
            cancelAnimator(this.mPreviewClipper);
            ValueAnimator animatorToRadius = getAnimatorToRadius(f);
            if (i == 0) {
                interpolator = Interpolators.FAST_OUT_LINEAR_IN;
            } else {
                interpolator = Interpolators.LINEAR_OUT_SLOW_IN;
            }
            animatorToRadius.setInterpolator(interpolator);
            long j = 250;
            if (!z) {
                j = Math.min((long) ((Math.abs(this.mCircleRadius - f) / ((float) this.mMinBackgroundRadius)) * 80.0f), 200L);
            }
            animatorToRadius.setDuration(j);
            animatorToRadius.start();
            View view2 = this.mPreviewView;
            if (view2 != null && view2.getVisibility() == 0) {
                this.mPreviewView.setVisibility(0);
                Animator createCircularReveal = ViewAnimationUtils.createCircularReveal(this.mPreviewView, getLeft() + this.mCenterX, getTop() + this.mCenterY, this.mCircleRadius, f);
                this.mPreviewClipper = createCircularReveal;
                createCircularReveal.setInterpolator(interpolator);
                this.mPreviewClipper.setDuration(j);
                this.mPreviewClipper.addListener(this.mClipEndListener);
                this.mPreviewClipper.addListener(new AnimatorListenerAdapter() {
                    /* class com.android.systemui.statusbar.KeyguardAffordanceView.AnonymousClass6 */

                    public void onAnimationEnd(Animator animator) {
                        KeyguardAffordanceView.this.mPreviewView.setVisibility(4);
                    }
                });
                this.mPreviewClipper.start();
            }
        }
    }

    private ValueAnimator getAnimatorToRadius(float f) {
        boolean z = false;
        ValueAnimator ofFloat = ValueAnimator.ofFloat(this.mCircleRadius, f);
        this.mCircleAnimator = ofFloat;
        this.mCircleStartValue = this.mCircleRadius;
        if (f == 0.0f) {
            z = true;
        }
        this.mCircleWillBeHidden = z;
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.systemui.statusbar.KeyguardAffordanceView.AnonymousClass7 */

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                KeyguardAffordanceView.this.mCircleRadius = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                KeyguardAffordanceView.this.updateIconColor();
                KeyguardAffordanceView.this.invalidate();
            }
        });
        ofFloat.addListener(this.mCircleEndListener);
        return ofFloat;
    }

    private void cancelAnimator(Animator animator) {
        if (animator != null) {
            animator.cancel();
        }
    }

    public void setImageScale(float f, boolean z) {
        setImageScale(f, z, -1, null);
    }

    public void setImageScale(float f, boolean z, long j, Interpolator interpolator) {
        cancelAnimator(this.mScaleAnimator);
        if (!z) {
            this.mImageScale = f;
            invalidate();
            return;
        }
        ValueAnimator ofFloat = ValueAnimator.ofFloat(this.mImageScale, f);
        this.mScaleAnimator = ofFloat;
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.systemui.statusbar.KeyguardAffordanceView.AnonymousClass8 */

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                KeyguardAffordanceView.this.mImageScale = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                KeyguardAffordanceView.this.invalidate();
            }
        });
        ofFloat.addListener(this.mScaleEndListener);
        if (interpolator == null) {
            if (f == 0.0f) {
                interpolator = Interpolators.FAST_OUT_LINEAR_IN;
            } else {
                interpolator = Interpolators.LINEAR_OUT_SLOW_IN;
            }
        }
        ofFloat.setInterpolator(interpolator);
        if (j == -1) {
            j = (long) (Math.min(1.0f, Math.abs(this.mImageScale - f) / 0.19999999f) * 200.0f);
        }
        ofFloat.setDuration(j);
        ofFloat.start();
    }

    public float getRestingAlpha() {
        return this.mRestingAlpha;
    }

    public void setImageAlpha(float f, boolean z) {
        setImageAlpha(f, z, -1, null, null);
    }

    public void setImageAlpha(float f, boolean z, long j, Interpolator interpolator, Runnable runnable) {
        Interpolator interpolator2;
        cancelAnimator(this.mAlphaAnimator);
        if (this.mLaunchingAffordance) {
            f = 0.0f;
        }
        int i = (int) (f * 255.0f);
        Drawable background = getBackground();
        if (!z) {
            if (background != null) {
                background.mutate().setAlpha(i);
            }
            setImageAlpha(i);
            return;
        }
        int imageAlpha = getImageAlpha();
        ValueAnimator ofInt = ValueAnimator.ofInt(imageAlpha, i);
        this.mAlphaAnimator = ofInt;
        ofInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(background) {
            /* class com.android.systemui.statusbar.$$Lambda$KeyguardAffordanceView$GLahQCZQtxFHfhh52YPyKQ2f5GE */
            public final /* synthetic */ Drawable f$1;

            {
                this.f$1 = r2;
            }

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                KeyguardAffordanceView.this.lambda$setImageAlpha$0$KeyguardAffordanceView(this.f$1, valueAnimator);
            }
        });
        ofInt.addListener(this.mAlphaEndListener);
        if (interpolator == null) {
            if (f == 0.0f) {
                interpolator2 = Interpolators.FAST_OUT_LINEAR_IN;
            } else {
                interpolator2 = Interpolators.LINEAR_OUT_SLOW_IN;
            }
            interpolator = interpolator2;
        }
        ofInt.setInterpolator(interpolator);
        if (j == -1) {
            j = (long) (Math.min(1.0f, ((float) Math.abs(imageAlpha - i)) / 255.0f) * 200.0f);
        }
        ofInt.setDuration(j);
        if (runnable != null) {
            ofInt.addListener(getEndListener(runnable));
        }
        ofInt.start();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setImageAlpha$0 */
    public /* synthetic */ void lambda$setImageAlpha$0$KeyguardAffordanceView(Drawable drawable, ValueAnimator valueAnimator) {
        int intValue = ((Integer) valueAnimator.getAnimatedValue()).intValue();
        if (drawable != null) {
            drawable.mutate().setAlpha(intValue);
        }
        setImageAlpha(intValue);
    }

    private Animator.AnimatorListener getEndListener(final Runnable runnable) {
        return new AnimatorListenerAdapter(this) {
            /* class com.android.systemui.statusbar.KeyguardAffordanceView.AnonymousClass9 */
            boolean mCancelled;

            public void onAnimationCancel(Animator animator) {
                this.mCancelled = true;
            }

            public void onAnimationEnd(Animator animator) {
                if (!this.mCancelled) {
                    runnable.run();
                }
            }
        };
    }

    public boolean performClick() {
        if (isClickable()) {
            return super.performClick();
        }
        return false;
    }

    public void setLaunchingAffordance(boolean z) {
        this.mLaunchingAffordance = z;
    }
}
