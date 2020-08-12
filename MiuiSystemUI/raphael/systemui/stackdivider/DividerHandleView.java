package com.android.systemui.stackdivider;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import android.view.animation.Interpolator;
import com.android.systemui.Interpolators;
import com.android.systemui.plugins.R;

public class DividerHandleView extends View {
    private static final Property<DividerHandleView, Integer> HEIGHT_PROPERTY = new Property<DividerHandleView, Integer>(Integer.class, "height") {
        public Integer get(DividerHandleView dividerHandleView) {
            return Integer.valueOf(dividerHandleView.mCurrentHeight);
        }

        public void set(DividerHandleView dividerHandleView, Integer num) {
            int unused = dividerHandleView.mCurrentHeight = num.intValue();
            dividerHandleView.invalidate();
        }
    };
    private static final Property<DividerHandleView, Integer> WIDTH_PROPERTY = new Property<DividerHandleView, Integer>(Integer.class, "width") {
        public Integer get(DividerHandleView dividerHandleView) {
            return Integer.valueOf(dividerHandleView.mCurrentWidth);
        }

        public void set(DividerHandleView dividerHandleView, Integer num) {
            int unused = dividerHandleView.mCurrentWidth = num.intValue();
            dividerHandleView.invalidate();
        }
    };
    /* access modifiers changed from: private */
    public AnimatorSet mAnimator;
    private final int mCircleDiameter;
    /* access modifiers changed from: private */
    public int mCurrentHeight;
    /* access modifiers changed from: private */
    public int mCurrentWidth;
    private final int mHeight;
    private final Paint mPaint = new Paint();
    private boolean mTouching;
    private final int mWidth;

    public boolean hasOverlappingRendering() {
        return false;
    }

    public DividerHandleView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mPaint.setColor(getResources().getColor(R.color.docked_divider_handle, (Resources.Theme) null));
        this.mPaint.setAntiAlias(true);
        this.mWidth = getResources().getDimensionPixelSize(R.dimen.docked_divider_handle_width);
        this.mHeight = getResources().getDimensionPixelSize(R.dimen.docked_divider_handle_height);
        int i = this.mWidth;
        this.mCurrentWidth = i;
        int i2 = this.mHeight;
        this.mCurrentHeight = i2;
        this.mCircleDiameter = (i + i2) / 3;
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
        ObjectAnimator ofInt = ObjectAnimator.ofInt(this, WIDTH_PROPERTY, new int[]{this.mCurrentWidth, i});
        ObjectAnimator ofInt2 = ObjectAnimator.ofInt(this, HEIGHT_PROPERTY, new int[]{this.mCurrentHeight, i2});
        this.mAnimator = new AnimatorSet();
        this.mAnimator.playTogether(new Animator[]{ofInt, ofInt2});
        this.mAnimator.setDuration(z ? 150 : 200);
        AnimatorSet animatorSet = this.mAnimator;
        if (z) {
            interpolator = Interpolators.TOUCH_RESPONSE;
        } else {
            interpolator = Interpolators.FAST_OUT_SLOW_IN;
        }
        animatorSet.setInterpolator(interpolator);
        this.mAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                AnimatorSet unused = DividerHandleView.this.mAnimator = null;
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
