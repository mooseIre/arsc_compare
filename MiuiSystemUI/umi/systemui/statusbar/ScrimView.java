package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;
import com.android.systemui.R$styleable;
import com.android.systemui.plugins.R;

public class ScrimView extends View {
    /* access modifiers changed from: private */
    public ValueAnimator mAlphaAnimator;
    private ValueAnimator.AnimatorUpdateListener mAlphaUpdateListener;
    private Runnable mChangeRunnable;
    private AnimatorListenerAdapter mClearAnimatorListener;
    private boolean mDrawAsSrc;
    private Rect mExcludedRect;
    private boolean mHasExcludedArea;
    private boolean mIsEmpty;
    private final Paint mPaint;
    private int mScrimColor;
    /* access modifiers changed from: private */
    public float mViewAlpha;

    public boolean hasOverlappingRendering() {
        return false;
    }

    public void setExcludedArea(Rect rect) {
    }

    public ScrimView(Context context) {
        this(context, (AttributeSet) null);
    }

    public ScrimView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ScrimView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public ScrimView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mPaint = new Paint();
        this.mIsEmpty = true;
        this.mViewAlpha = 1.0f;
        this.mExcludedRect = new Rect();
        this.mAlphaUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float unused = ScrimView.this.mViewAlpha = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                ScrimView.this.invalidate();
            }
        };
        this.mClearAnimatorListener = new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                ValueAnimator unused = ScrimView.this.mAlphaAnimator = null;
            }
        };
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.ScrimView);
        try {
            this.mScrimColor = obtainStyledAttributes.getColor(0, -16777216);
        } finally {
            obtainStyledAttributes.recycle();
        }
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        if (this.mDrawAsSrc || (!this.mIsEmpty && this.mViewAlpha > 0.0f)) {
            PorterDuff.Mode mode = this.mDrawAsSrc ? PorterDuff.Mode.SRC : PorterDuff.Mode.SRC_OVER;
            int scrimColorWithAlpha = getScrimColorWithAlpha();
            if (!this.mHasExcludedArea) {
                canvas.drawColor(scrimColorWithAlpha, mode);
                return;
            }
            this.mPaint.setColor(scrimColorWithAlpha);
            if (this.mExcludedRect.top > 0) {
                canvas.drawRect(0.0f, 0.0f, (float) getWidth(), (float) this.mExcludedRect.top, this.mPaint);
            }
            Rect rect = this.mExcludedRect;
            int i = rect.left;
            if (i > 0) {
                canvas.drawRect(0.0f, (float) rect.top, (float) i, (float) rect.bottom, this.mPaint);
            }
            if (this.mExcludedRect.right < getWidth()) {
                Rect rect2 = this.mExcludedRect;
                canvas.drawRect((float) rect2.right, (float) rect2.top, (float) getWidth(), (float) this.mExcludedRect.bottom, this.mPaint);
            }
            if (this.mExcludedRect.bottom < getHeight()) {
                canvas.drawRect(0.0f, (float) this.mExcludedRect.bottom, (float) getWidth(), (float) getHeight(), this.mPaint);
            }
        }
    }

    public int getScrimColorWithAlpha() {
        if (getId() != R.id.scrim_in_front) {
            return 0;
        }
        int i = this.mScrimColor;
        return Color.argb((int) (((float) Color.alpha(i)) * this.mViewAlpha), Color.red(i), Color.green(i), Color.blue(i));
    }

    public void setDrawAsSrc(boolean z) {
        PorterDuff.Mode mode;
        this.mDrawAsSrc = z;
        Paint paint = this.mPaint;
        if (this.mDrawAsSrc) {
            mode = PorterDuff.Mode.SRC;
        } else {
            mode = PorterDuff.Mode.SRC_OVER;
        }
        paint.setXfermode(new PorterDuffXfermode(mode));
        invalidate();
    }

    public void setScrimColor(int i) {
        if (getId() == R.id.scrim_in_front && i != this.mScrimColor) {
            this.mIsEmpty = Color.alpha(i) == 0;
            this.mScrimColor = i;
            invalidate();
            Runnable runnable = this.mChangeRunnable;
            if (runnable != null) {
                runnable.run();
            }
        }
    }

    public int getScrimColor() {
        return this.mScrimColor;
    }

    public void animateViewAlpha(float f, long j, Interpolator interpolator) {
        ValueAnimator valueAnimator = this.mAlphaAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{this.mViewAlpha, f});
        this.mAlphaAnimator = ofFloat;
        ofFloat.addUpdateListener(this.mAlphaUpdateListener);
        this.mAlphaAnimator.addListener(this.mClearAnimatorListener);
        this.mAlphaAnimator.setInterpolator(interpolator);
        this.mAlphaAnimator.setDuration(j);
        this.mAlphaAnimator.start();
    }

    public void setChangeRunnable(Runnable runnable) {
        this.mChangeRunnable = runnable;
    }
}
