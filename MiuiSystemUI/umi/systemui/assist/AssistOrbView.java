package com.android.systemui.assist;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.Interpolators;

public class AssistOrbView extends FrameLayout {
    private final Paint mBackgroundPaint;
    private final int mBaseMargin;
    private float mCircleAnimationEndValue;
    private ValueAnimator mCircleAnimator;
    private final int mCircleMinSize;
    private final Rect mCircleRect;
    private float mCircleSize;
    private ValueAnimator.AnimatorUpdateListener mCircleUpdateListener;
    private AnimatorListenerAdapter mClearAnimatorListener;
    private boolean mClipToOutline;
    private ImageView mLogo;
    private final int mMaxElevation;
    private float mOffset;
    private ValueAnimator mOffsetAnimator;
    private ValueAnimator.AnimatorUpdateListener mOffsetUpdateListener;
    private float mOutlineAlpha;
    private final Interpolator mOvershootInterpolator;
    private final int mStaticOffset;
    private final Rect mStaticRect;

    public boolean hasOverlappingRendering() {
        return false;
    }

    public AssistOrbView(Context context) {
        this(context, null);
    }

    public AssistOrbView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AssistOrbView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public AssistOrbView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mBackgroundPaint = new Paint();
        this.mCircleRect = new Rect();
        this.mStaticRect = new Rect();
        this.mOvershootInterpolator = new OvershootInterpolator();
        this.mCircleUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.systemui.assist.AssistOrbView.AnonymousClass1 */

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                AssistOrbView.this.applyCircleSize(((Float) valueAnimator.getAnimatedValue()).floatValue());
                AssistOrbView.this.updateElevation();
            }
        };
        this.mClearAnimatorListener = new AnimatorListenerAdapter() {
            /* class com.android.systemui.assist.AssistOrbView.AnonymousClass2 */

            public void onAnimationEnd(Animator animator) {
                AssistOrbView.this.mCircleAnimator = null;
            }
        };
        this.mOffsetUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.systemui.assist.AssistOrbView.AnonymousClass3 */

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                AssistOrbView.this.mOffset = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                AssistOrbView.this.updateLayout();
            }
        };
        setOutlineProvider(new ViewOutlineProvider() {
            /* class com.android.systemui.assist.AssistOrbView.AnonymousClass4 */

            public void getOutline(View view, Outline outline) {
                if (AssistOrbView.this.mCircleSize > 0.0f) {
                    outline.setOval(AssistOrbView.this.mCircleRect);
                } else {
                    outline.setEmpty();
                }
                outline.setAlpha(AssistOrbView.this.mOutlineAlpha);
            }
        });
        setWillNotDraw(false);
        this.mCircleMinSize = context.getResources().getDimensionPixelSize(C0012R$dimen.assist_orb_size);
        this.mBaseMargin = context.getResources().getDimensionPixelSize(C0012R$dimen.assist_orb_base_margin);
        this.mStaticOffset = context.getResources().getDimensionPixelSize(C0012R$dimen.assist_orb_travel_distance);
        this.mMaxElevation = context.getResources().getDimensionPixelSize(C0012R$dimen.assist_orb_elevation);
        this.mBackgroundPaint.setAntiAlias(true);
        this.mBackgroundPaint.setColor(getResources().getColor(C0011R$color.assist_orb_color));
    }

    public ImageView getLogo() {
        return this.mLogo;
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
    }

    private void drawBackground(Canvas canvas) {
        canvas.drawCircle((float) this.mCircleRect.centerX(), (float) this.mCircleRect.centerY(), this.mCircleSize / 2.0f, this.mBackgroundPaint);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mLogo = (ImageView) findViewById(C0015R$id.search_logo);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        ImageView imageView = this.mLogo;
        imageView.layout(0, 0, imageView.getMeasuredWidth(), this.mLogo.getMeasuredHeight());
        if (z) {
            updateCircleRect(this.mStaticRect, (float) this.mStaticOffset, true);
        }
    }

    public void animateCircleSize(float f, long j, long j2, Interpolator interpolator) {
        if (f != this.mCircleAnimationEndValue) {
            ValueAnimator valueAnimator = this.mCircleAnimator;
            if (valueAnimator != null) {
                valueAnimator.cancel();
            }
            ValueAnimator ofFloat = ValueAnimator.ofFloat(this.mCircleSize, f);
            this.mCircleAnimator = ofFloat;
            ofFloat.addUpdateListener(this.mCircleUpdateListener);
            this.mCircleAnimator.addListener(this.mClearAnimatorListener);
            this.mCircleAnimator.setInterpolator(interpolator);
            this.mCircleAnimator.setDuration(j);
            this.mCircleAnimator.setStartDelay(j2);
            this.mCircleAnimator.start();
            this.mCircleAnimationEndValue = f;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void applyCircleSize(float f) {
        this.mCircleSize = f;
        updateLayout();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateElevation() {
        int i = this.mStaticOffset;
        setElevation((1.0f - Math.max((((float) i) - this.mOffset) / ((float) i), 0.0f)) * ((float) this.mMaxElevation));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void animateOffset(float f, long j, long j2, Interpolator interpolator) {
        ValueAnimator valueAnimator = this.mOffsetAnimator;
        if (valueAnimator != null) {
            valueAnimator.removeAllListeners();
            this.mOffsetAnimator.cancel();
        }
        ValueAnimator ofFloat = ValueAnimator.ofFloat(this.mOffset, f);
        this.mOffsetAnimator = ofFloat;
        ofFloat.addUpdateListener(this.mOffsetUpdateListener);
        this.mOffsetAnimator.addListener(new AnimatorListenerAdapter() {
            /* class com.android.systemui.assist.AssistOrbView.AnonymousClass5 */

            public void onAnimationEnd(Animator animator) {
                AssistOrbView.this.mOffsetAnimator = null;
            }
        });
        this.mOffsetAnimator.setInterpolator(interpolator);
        this.mOffsetAnimator.setStartDelay(j2);
        this.mOffsetAnimator.setDuration(j);
        this.mOffsetAnimator.start();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateLayout() {
        updateCircleRect();
        updateLogo();
        invalidateOutline();
        invalidate();
        updateClipping();
    }

    private void updateClipping() {
        boolean z = this.mCircleSize < ((float) this.mCircleMinSize);
        if (z != this.mClipToOutline) {
            setClipToOutline(z);
            this.mClipToOutline = z;
        }
    }

    private void updateLogo() {
        Rect rect = this.mCircleRect;
        float width = (((float) (rect.left + rect.right)) / 2.0f) - (((float) this.mLogo.getWidth()) / 2.0f);
        Rect rect2 = this.mCircleRect;
        float height = ((((float) (rect2.top + rect2.bottom)) / 2.0f) - (((float) this.mLogo.getHeight()) / 2.0f)) - (((float) this.mCircleMinSize) / 7.0f);
        int i = this.mStaticOffset;
        float f = (((float) i) - this.mOffset) / ((float) i);
        this.mLogo.setImageAlpha((int) (Math.max(((1.0f - f) - 0.5f) * 2.0f, 0.0f) * 255.0f));
        this.mLogo.setTranslationX(width);
        this.mLogo.setTranslationY(height + (((float) i) * f * 0.1f));
    }

    private void updateCircleRect() {
        updateCircleRect(this.mCircleRect, this.mOffset, false);
    }

    private void updateCircleRect(Rect rect, float f, boolean z) {
        float f2 = z ? (float) this.mCircleMinSize : this.mCircleSize;
        int width = ((int) (((float) getWidth()) - f2)) / 2;
        int height = (int) (((((float) getHeight()) - (f2 / 2.0f)) - ((float) this.mBaseMargin)) - f);
        rect.set(width, height, (int) (((float) width) + f2), (int) (((float) height) + f2));
    }

    public void startExitAnimation(long j) {
        animateCircleSize(0.0f, 200, j, Interpolators.FAST_OUT_LINEAR_IN);
        animateOffset(0.0f, 200, j, Interpolators.FAST_OUT_LINEAR_IN);
    }

    public void startEnterAnimation() {
        applyCircleSize(0.0f);
        post(new Runnable() {
            /* class com.android.systemui.assist.AssistOrbView.AnonymousClass6 */

            public void run() {
                AssistOrbView assistOrbView = AssistOrbView.this;
                assistOrbView.animateCircleSize((float) assistOrbView.mCircleMinSize, 300, 0, AssistOrbView.this.mOvershootInterpolator);
                AssistOrbView assistOrbView2 = AssistOrbView.this;
                assistOrbView2.animateOffset((float) assistOrbView2.mStaticOffset, 400, 0, Interpolators.LINEAR_OUT_SLOW_IN);
            }
        });
    }

    public void reset() {
        this.mClipToOutline = false;
        this.mBackgroundPaint.setAlpha(255);
        this.mOutlineAlpha = 1.0f;
    }
}
