package com.android.keyguard.fod;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;
import android.view.animation.LinearInterpolator;
import com.android.systemui.C0012R$dimen;

class MiuiGxzwQuickTeachView extends View {
    private float mCicleRadius = getContext().getResources().getDimension(C0012R$dimen.gxzw_quick_open_circle_radius);
    private float mItemRadius;
    private Paint mPaint;
    private ValueAnimator mValueAnimator;

    public MiuiGxzwQuickTeachView(Context context, float f) {
        super(context);
        this.mItemRadius = f;
        Paint paint = new Paint();
        this.mPaint = paint;
        paint.setAntiAlias(true);
        this.mPaint.setStrokeCap(Paint.Cap.ROUND);
        this.mPaint.setStrokeWidth(this.mItemRadius * 2.0f);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        ValueAnimator valueAnimator = this.mValueAnimator;
        if (valueAnimator != null && valueAnimator.isRunning()) {
            float floatValue = ((Float) this.mValueAnimator.getAnimatedValue()).floatValue();
            float f = this.mCicleRadius;
            if (floatValue <= f * 2.0f) {
                if (floatValue > f) {
                    floatValue = f;
                } else if (floatValue < 0.0f) {
                    floatValue = 0.0f;
                }
                this.mPaint.setStyle(Paint.Style.STROKE);
                float f2 = this.mItemRadius;
                RectF rectF = new RectF(0.0f, floatValue + f2, 2.0f * f2, this.mCicleRadius + f2);
                this.mPaint.setShader(new LinearGradient(rectF.centerX(), rectF.top, rectF.centerX(), rectF.bottom + this.mItemRadius, -13264897, 3512319, Shader.TileMode.CLAMP));
                canvas.drawLine(rectF.centerX(), rectF.top, rectF.centerX(), rectF.bottom, this.mPaint);
                this.mPaint.setShader(null);
                this.mPaint.setStyle(Paint.Style.FILL);
                this.mPaint.setColor(-13264897);
                float f3 = this.mItemRadius;
                canvas.drawCircle(f3, floatValue + f3, f3, this.mPaint);
            }
        }
    }

    public void startTeachAnim() {
        ValueAnimator valueAnimator = this.mValueAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        float f = this.mCicleRadius;
        ValueAnimator ofFloat = ValueAnimator.ofFloat(3.0f * f, -f);
        this.mValueAnimator = ofFloat;
        ofFloat.setDuration(2000L);
        this.mValueAnimator.setInterpolator(new LinearInterpolator());
        this.mValueAnimator.setRepeatMode(1);
        this.mValueAnimator.setRepeatCount(-1);
        this.mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.keyguard.fod.MiuiGxzwQuickTeachView.AnonymousClass1 */

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiGxzwQuickTeachView.this.invalidate();
            }
        });
        this.mValueAnimator.start();
        invalidate();
    }

    public void stopTeachAnim() {
        ValueAnimator valueAnimator = this.mValueAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        this.mValueAnimator = null;
        invalidate();
    }
}
