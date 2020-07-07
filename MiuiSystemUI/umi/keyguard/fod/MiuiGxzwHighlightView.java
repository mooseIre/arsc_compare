package com.android.keyguard.fod;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import com.android.systemui.plugins.R;

class MiuiGxzwHighlightView extends ImageView {
    private int mCircleColor;
    private boolean mGradualGreenCircle = false;
    private boolean mInvertColor = false;
    private float mOvalAngle;
    private float mOvalMajor;
    private float mOvalMinor;
    private Paint mPaint;
    private boolean mSupportHalo = false;
    private float mTouchCenterX = 0.0f;
    private float mTouchCenterY = 0.0f;

    public MiuiGxzwHighlightView(Context context) {
        super(context);
    }

    public MiuiGxzwHighlightView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public MiuiGxzwHighlightView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public void setTouchCenter(float f, float f2) {
        this.mTouchCenterX = f;
        this.mTouchCenterY = f2;
        invalidate();
    }

    public void setOvalInfo(float f, float f2, float f3) {
        this.mOvalAngle = f;
        this.mOvalMajor = f2;
        this.mOvalMinor = f3;
        invalidate();
    }

    public void setVisibility(int i) {
        Log.i("MiuiGxzwHighlightView", "setVisibility: visibility = " + i);
        super.setVisibility(i);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mGradualGreenCircle) {
            return;
        }
        if (this.mSupportHalo && !this.mInvertColor) {
            return;
        }
        if (MiuiGxzwUtils.isLargeFod()) {
            canvas.save();
            canvas.rotate((float) ((int) (((double) (this.mOvalAngle * 180.0f)) / 3.141592653589793d)), this.mTouchCenterX, this.mTouchCenterY);
            float f = this.mTouchCenterX;
            float f2 = this.mOvalMinor;
            float f3 = this.mTouchCenterY;
            float f4 = this.mOvalMajor;
            canvas.drawOval(new RectF(f - (f2 / 2.0f), f3 - (f4 / 2.0f), f + (f2 / 2.0f), f3 + (f4 / 2.0f)), this.mPaint);
            canvas.restore();
            return;
        }
        canvas.drawCircle((float) (getWidth() / 2), (float) (getHeight() / 2), (float) (Math.min(MiuiGxzwUtils.GXZW_ICON_WIDTH, MiuiGxzwUtils.GXZW_ICON_HEIGHT) / 2), this.mPaint);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        initView();
    }

    private void initView() {
        this.mCircleColor = getContext().getResources().getColor(R.color.gxzw_circle_color);
        this.mGradualGreenCircle = getContext().getResources().getBoolean(R.bool.config_enableGradualGreenCircle);
        this.mSupportHalo = MiuiGxzwUtils.supportHalo(getContext());
        Paint paint = new Paint();
        this.mPaint = paint;
        paint.setColor(this.mCircleColor);
        this.mPaint.setAntiAlias(true);
        if (this.mGradualGreenCircle) {
            setImageResource(R.drawable.gxzw_green_light);
        } else if (this.mSupportHalo) {
            setImageResource(MiuiGxzwUtils.getHaloRes());
        }
        setScaleType(ImageView.ScaleType.FIT_XY);
    }

    public void setInvertColorStatus(boolean z) {
        this.mInvertColor = z;
        if (!z || this.mCircleColor != -1) {
            this.mPaint.setColor(this.mCircleColor);
        } else {
            this.mPaint.setColor(-16777216);
        }
        if (this.mSupportHalo && z) {
            setImageDrawable((Drawable) null);
        } else if (this.mSupportHalo) {
            setImageResource(MiuiGxzwUtils.getHaloRes());
        }
        invalidate();
    }
}
