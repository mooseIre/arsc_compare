package com.android.systemui.views;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import com.android.systemui.C0011R$color;
import com.android.systemui.util.ViewAnimUtils;
import miui.view.animation.CubicEaseOutInterpolator;

public class CircleAndTickAnimView extends View {
    private boolean isNormalDrawableShow;
    private AnimatorSet mAnimatorSet;
    private Drawable mBackDrawable;
    private ValueAnimator mCircleAnimator;
    private float mCircleRotateDegrees;
    private int mDiameter;
    private Drawable mNormalDrawable;
    private final Rect mOutRect;
    private Path mTickDstPath;
    private float mTickEndPoint;
    private ValueAnimator mTickEndPointAnimator;
    private Paint mTickPaint;
    private float mTickPathLength;
    private PathMeasure mTickPathMeasure;
    private float mTickStartPoint;
    private ValueAnimator mTickStartPointAnimator;
    private final Rect mViewRect;

    public CircleAndTickAnimView(Context context) {
        this(context, null);
    }

    public CircleAndTickAnimView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public CircleAndTickAnimView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mAnimatorSet = new AnimatorSet();
        this.isNormalDrawableShow = true;
        this.mViewRect = new Rect();
        this.mOutRect = new Rect();
        this.mTickPathMeasure = new PathMeasure();
        this.mTickDstPath = new Path();
        this.mTickPaint = new Paint();
        initAnimator();
        initTickPaint(context);
        stopAnimator();
        ViewAnimUtils.mouse(this);
    }

    private void initAnimator() {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, -90.0f);
        this.mCircleAnimator = ofFloat;
        ofFloat.setDuration(200L);
        this.mCircleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.systemui.views.CircleAndTickAnimView.AnonymousClass1 */

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                CircleAndTickAnimView.this.mCircleRotateDegrees = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                CircleAndTickAnimView.this.invalidate();
            }
        });
        ValueAnimator ofFloat2 = ValueAnimator.ofFloat(0.0f, 0.31f);
        this.mTickStartPointAnimator = ofFloat2;
        ofFloat2.setInterpolator(new CubicEaseOutInterpolator());
        this.mTickStartPointAnimator.setStartDelay(50);
        this.mTickStartPointAnimator.setDuration(250L);
        this.mTickStartPointAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.systemui.views.CircleAndTickAnimView.AnonymousClass2 */

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                CircleAndTickAnimView.this.mTickStartPoint = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                CircleAndTickAnimView.this.invalidate();
            }
        });
        ValueAnimator ofFloat3 = ValueAnimator.ofFloat(0.0f, 1.0f);
        this.mTickEndPointAnimator = ofFloat3;
        ofFloat3.setInterpolator(new CubicEaseOutInterpolator());
        this.mTickEndPointAnimator.setDuration(300L);
        this.mTickEndPointAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.systemui.views.CircleAndTickAnimView.AnonymousClass3 */

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                CircleAndTickAnimView.this.mTickEndPoint = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                CircleAndTickAnimView.this.invalidate();
            }
        });
        this.mAnimatorSet.play(this.mTickStartPointAnimator).with(this.mTickEndPointAnimator).after(this.mCircleAnimator);
    }

    private void initTickPaint(Context context) {
        this.mTickPaint.setStyle(Paint.Style.STROKE);
        this.mTickPaint.setStrokeWidth(5.0f);
        this.mTickPaint.setStrokeCap(Paint.Cap.ROUND);
        this.mTickPaint.setStrokeJoin(Paint.Join.ROUND);
        this.mTickPaint.setAntiAlias(true);
        this.mTickPaint.setColor(context.getColor(C0011R$color.recent_tick_anim_color));
    }

    private void initRightMarkPath() {
        Path path = new Path();
        int i = this.mDiameter;
        path.moveTo(((float) i) * 0.27f, ((float) i) * 0.4f);
        int i2 = this.mDiameter;
        path.lineTo(((float) i2) * 0.46f, ((float) i2) * 0.58f);
        int i3 = this.mDiameter;
        path.lineTo(((float) i3) * 0.62f, ((float) i3) * 0.42f);
        this.mTickPathMeasure.setPath(path, false);
        this.mTickPathLength = this.mTickPathMeasure.getLength();
    }

    public void animatorStart(Animator.AnimatorListener animatorListener) {
        stopAnimator();
        this.isNormalDrawableShow = false;
        this.mAnimatorSet.addListener(animatorListener);
        this.mAnimatorSet.start();
    }

    public void setBackDrawable(int i) {
        setBackDrawable(getDrawable(i));
    }

    public void setBackDrawable(Drawable drawable) {
        this.mBackDrawable = drawable;
        setBackground(drawable);
    }

    public void setNormalDrawable(int i) {
        setNormalDrawable(getDrawable(i));
    }

    public void setNormalDrawable(Drawable drawable) {
        this.mNormalDrawable = drawable;
    }

    private Drawable getDrawable(int i) {
        Drawable drawable = getContext().getResources().getDrawable(i);
        if (drawable == null) {
            return null;
        }
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.mutate();
        return drawable;
    }

    public void setDrawables(int i, int i2) {
        setNormalDrawable(i);
        setBackDrawable(i2);
        int min = Math.min(getIntrinsicWidth(), getIntrinsicHeight());
        this.mDiameter = min;
        this.mViewRect.set(0, 0, min, min);
        initRightMarkPath();
    }

    public void stopAnimator() {
        AnimatorSet animatorSet = this.mAnimatorSet;
        if (animatorSet != null && animatorSet.isRunning()) {
            this.mAnimatorSet.cancel();
        }
        resetAnimator();
    }

    public void resetAnimator() {
        this.mAnimatorSet.removeAllListeners();
        this.mCircleRotateDegrees = 0.0f;
        this.mTickStartPoint = 0.0f;
        this.mTickEndPoint = 0.0f;
        this.isNormalDrawableShow = true;
    }

    /* access modifiers changed from: protected */
    public void drawableStateChanged() {
        super.drawableStateChanged();
        Drawable drawable = this.mBackDrawable;
        if (drawable != null) {
            drawable.setState(getDrawableState());
        }
        invalidate();
    }

    private int getIntrinsicWidth() {
        Drawable drawable = this.mNormalDrawable;
        int i = 0;
        if (drawable != null) {
            i = Math.max(0, drawable.getIntrinsicHeight());
        }
        Drawable drawable2 = this.mBackDrawable;
        return drawable2 != null ? Math.max(i, drawable2.getIntrinsicHeight()) : i;
    }

    private int getIntrinsicHeight() {
        Drawable drawable = this.mNormalDrawable;
        int i = 0;
        if (drawable != null) {
            i = Math.max(0, drawable.getIntrinsicHeight());
        }
        Drawable drawable2 = this.mBackDrawable;
        return drawable2 != null ? Math.max(i, drawable2.getIntrinsicHeight()) : i;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        int i3 = this.mDiameter;
        setMeasuredDimension(i3, i3);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        Drawable drawable = this.mNormalDrawable;
        if (drawable != null) {
            Gravity.apply(17, drawable.getIntrinsicWidth(), this.mNormalDrawable.getIntrinsicHeight(), this.mViewRect, this.mOutRect);
            this.mNormalDrawable.setBounds(this.mOutRect);
            if (this.isNormalDrawableShow) {
                this.mNormalDrawable.setAlpha(255);
                this.mNormalDrawable.draw(canvas);
            } else {
                this.mNormalDrawable.setAlpha((int) (((this.mCircleRotateDegrees / 90.0f) + 1.0f) * 255.0f));
                canvas.save();
                float f = this.mCircleRotateDegrees;
                int i = this.mDiameter;
                canvas.rotate(f, ((float) i) / 2.0f, ((float) i) / 2.0f);
                this.mNormalDrawable.draw(canvas);
                canvas.restore();
            }
        }
        canvas.saveLayer(null, null);
        this.mTickDstPath.reset();
        float f2 = this.mTickPathLength;
        this.mTickPathMeasure.getSegment(this.mTickStartPoint * f2, f2 * this.mTickEndPoint, this.mTickDstPath, true);
        canvas.drawPath(this.mTickDstPath, this.mTickPaint);
        canvas.restore();
    }
}
