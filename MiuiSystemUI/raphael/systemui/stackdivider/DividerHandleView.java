package com.android.systemui.stackdivider;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import android.view.animation.Interpolator;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.Interpolators;

public class DividerHandleView extends View {
    private static final Property<DividerHandleView, Integer> HEIGHT_PROPERTY = new Property<DividerHandleView, Integer>(Integer.class, "height") {
        /* class com.android.systemui.stackdivider.DividerHandleView.AnonymousClass2 */

        public Integer get(DividerHandleView dividerHandleView) {
            return Integer.valueOf(dividerHandleView.mCurrentHeight);
        }

        public void set(DividerHandleView dividerHandleView, Integer num) {
            dividerHandleView.mCurrentHeight = num.intValue();
            dividerHandleView.invalidate();
        }
    };
    private static final Property<DividerHandleView, Integer> WIDTH_PROPERTY = new Property<DividerHandleView, Integer>(Integer.class, "width") {
        /* class com.android.systemui.stackdivider.DividerHandleView.AnonymousClass1 */

        public Integer get(DividerHandleView dividerHandleView) {
            return Integer.valueOf(dividerHandleView.mCurrentWidth);
        }

        public void set(DividerHandleView dividerHandleView, Integer num) {
            dividerHandleView.mCurrentWidth = num.intValue();
            dividerHandleView.invalidate();
        }
    };
    private AnimatorSet mAnimator;
    private final int mCircleDiameter;
    private int mCurrentHeight;
    private int mCurrentWidth;
    private final int mHeight;
    private final Paint mPaint;
    private boolean mTouching;
    private final int mWidth = getResources().getDimensionPixelSize(C0012R$dimen.docked_divider_handle_width);

    public boolean hasOverlappingRendering() {
        return false;
    }

    public DividerHandleView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        Paint paint = new Paint();
        this.mPaint = paint;
        paint.setColor(getResources().getColor(C0011R$color.docked_divider_handle, null));
        this.mPaint.setAntiAlias(true);
        int dimensionPixelSize = getResources().getDimensionPixelSize(C0012R$dimen.docked_divider_handle_height);
        this.mHeight = dimensionPixelSize;
        int i = this.mWidth;
        this.mCurrentWidth = i;
        this.mCurrentHeight = dimensionPixelSize;
        this.mCircleDiameter = (i + dimensionPixelSize) / 3;
    }

    public void setTouching(boolean z, boolean z2) {
        if (z != this.mTouching) {
            AnimatorSet animatorSet = this.mAnimator;
            if (animatorSet != null) {
                animatorSet.cancel();
                this.mAnimator = null;
            }
            if (!z2) {
                if (z) {
                    int i = this.mCircleDiameter;
                    this.mCurrentWidth = i;
                    this.mCurrentHeight = i;
                } else {
                    this.mCurrentWidth = this.mWidth;
                    this.mCurrentHeight = this.mHeight;
                }
                invalidate();
            } else {
                animateToTarget(z ? this.mCircleDiameter : this.mWidth, z ? this.mCircleDiameter : this.mHeight, z);
            }
            this.mTouching = z;
        }
    }

    private void animateToTarget(int i, int i2, boolean z) {
        Interpolator interpolator;
        ObjectAnimator ofInt = ObjectAnimator.ofInt(this, WIDTH_PROPERTY, this.mCurrentWidth, i);
        ObjectAnimator ofInt2 = ObjectAnimator.ofInt(this, HEIGHT_PROPERTY, this.mCurrentHeight, i2);
        AnimatorSet animatorSet = new AnimatorSet();
        this.mAnimator = animatorSet;
        animatorSet.playTogether(ofInt, ofInt2);
        this.mAnimator.setDuration(z ? 150 : 200);
        AnimatorSet animatorSet2 = this.mAnimator;
        if (z) {
            interpolator = Interpolators.TOUCH_RESPONSE;
        } else {
            interpolator = Interpolators.FAST_OUT_SLOW_IN;
        }
        animatorSet2.setInterpolator(interpolator);
        this.mAnimator.addListener(new AnimatorListenerAdapter() {
            /* class com.android.systemui.stackdivider.DividerHandleView.AnonymousClass3 */

            public void onAnimationEnd(Animator animator) {
                DividerHandleView.this.mAnimator = null;
            }
        });
        this.mAnimator.start();
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        int width = (getWidth() / 2) - (this.mCurrentWidth / 2);
        int i = this.mCurrentHeight;
        int height = (getHeight() / 2) - (i / 2);
        float min = (float) (Math.min(this.mCurrentWidth, i) / 2);
        canvas.drawRoundRect((float) width, (float) height, (float) (width + this.mCurrentWidth), (float) (height + this.mCurrentHeight), min, min, this.mPaint);
    }
}
