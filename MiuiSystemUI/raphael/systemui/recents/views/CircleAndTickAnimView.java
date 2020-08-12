package com.android.systemui.recents.views;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import com.android.systemui.plugins.R;
import com.android.systemui.util.ViewAnimUtils;
import miui.view.animation.CubicEaseOutInterpolator;

public class CircleAndTickAnimView extends View {
    private boolean isNormalDrawableShow;
    private AnimatorSet mAnimatorSet;
    private Drawable mBackDrawable;
    private ValueAnimator mCircleAnimator;
    /* access modifiers changed from: private */
    public float mCircleRotateDegrees;
    private int mDiameter;
    private Drawable mNormalDrawable;
    private Path mTickDstPath;
    /* access modifiers changed from: private */
    public float mTickEndPoint;
    private ValueAnimator mTickEndPointAnimator;
    private Paint mTickPaint;
    private float mTickPathLength;
    private PathMeasure mTickPathMeasure;
    /* access modifiers changed from: private */
    public float mTickStartPoint;
    private ValueAnimator mTickStartPointAnimator;
    private Rect mViewRect;

    public CircleAndTickAnimView(Context context) {
        this(context, (AttributeSet) null);
    }

    public CircleAndTickAnimView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public CircleAndTickAnimView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mAnimatorSet = new AnimatorSet();
        this.isNormalDrawableShow = true;
        this.mViewRect = new Rect();
        this.mTickPathMeasure = new PathMeasure();
        this.mTickDstPath = new Path();
        this.mTickPaint = new Paint();
        initAnimator();
        initTickPaint(context);
        stopAnimator();
        ViewAnimUtils.mouse(this);
    }

    private void initAnimator() {
        this.mCircleAnimator = ValueAnimator.ofFloat(new float[]{0.0f, -90.0f});
        this.mCircleAnimator.setDuration(200);
        this.mCircleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float unused = CircleAndTickAnimView.this.mCircleRotateDegrees = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                CircleAndTickAnimView.this.invalidate();
            }
        });
        this.mTickStartPointAnimator = ValueAnimator.ofFloat(new float[]{0.0f, 0.31f});
        this.mTickStartPointAnimator.setInterpolator(new CubicEaseOutInterpolator());
        this.mTickStartPointAnimator.setStartDelay(50);
        this.mTickStartPointAnimator.setDuration(250);
        this.mTickStartPointAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float unused = CircleAndTickAnimView.this.mTickStartPoint = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                CircleAndTickAnimView.this.invalidate();
            }
        });
        this.mTickEndPointAnimator = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        this.mTickEndPointAnimator.setInterpolator(new CubicEaseOutInterpolator());
        this.mTickEndPointAnimator.setDuration(300);
        this.mTickEndPointAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float unused = CircleAndTickAnimView.this.mTickEndPoint = ((Float) valueAnimator.getAnimatedValue()).floatValue();
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
        this.mTickPaint.setColor(context.getColor(R.color.recent_tick_anim_color));
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
        this.mDiameter = Math.min(getIntrinsicWidth(), getIntrinsicHeight());
        Rect rect = this.mViewRect;
        int i3 = this.mDiameter;
        rect.set(0, 0, i3, i3);
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
        Rect rect = new Rect();
        Drawable drawable = this.mNormalDrawable;
        if (drawable != null) {
            Gravity.apply(17, drawable.getIntrinsicWidth(), this.mNormalDrawable.getIntrinsicHeight(), this.mViewRect, rect);
            this.mNormalDrawable.setBounds(rect);
            if (this.isNormalDrawableShow) {
                this.mNormalDrawable.setAlpha((int) (getAlpha() * 255.0f));
                this.mNormalDrawable.draw(canvas);
            } else {
                this.mNormalDrawable.setAlpha((int) (((this.mCircleRotateDegrees / 90.0f) + 1.0f) * 255.0f));
                canvas.save();
                float f = this.mCircleRotateDegrees;
                int i = this.mDiameter;
                canvas.rotate(f, (float) (i / 2), (float) (i / 2));
                this.mNormalDrawable.draw(canvas);
                canvas.restore();
            }
        }
        canvas.saveLayer((RectF) null, (Paint) null);
        this.mTickDstPath.reset();
        float f2 = this.mTickPathLength;
        this.mTickPathMeasure.getSegment(this.mTickStartPoint * f2, f2 * this.mTickEndPoint, this.mTickDstPath, true);
        canvas.drawPath(this.mTickDstPath, this.mTickPaint);
        canvas.restore();
    }
}
