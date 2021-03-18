package com.android.keyguard.charge.lollipop;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import androidx.constraintlayout.widget.R$styleable;
import com.android.keyguard.charge.Constants;

public class OutlineView extends View {
    private static final int OUTER_CIRCLE_END_COLOR = Color.parseColor("#3216a5");
    private static final int OUTER_CIRCLE_MIDDLE_COLOR = Color.parseColor("#0e5dff");
    private static final int OUTER_CIRCLE_START_COLOR = Color.parseColor("#d013ff");
    private float mArcAngleDegree;
    private float mArcCircleCenterY;
    private float mArcCircleRadius;
    private float mArcLeftCircleCenterX;
    private float mArcRightCircleCenterX;
    private Paint mOutCirclePaint;
    private Paint mOutSecCirclePaint;
    private Paint mOutThrCirclePaint;
    private int mOuterCircleCenterX;
    private int mOuterCircleCenterY;
    private int mOuterCircleRadius;
    private int mOuterCircleWidth;
    private int mOuterSecCircleRadius;
    private int mOuterSecCircleWidth;
    private int mOuterThrCircleRadius;
    private int mOuterThrCircleWidth;
    private Point mScreenSize;
    private float mSecArcAngleDegree;
    private float mSecArcCircleCenterY;
    private float mSecArcCircleRadius;
    private float mSecArcLeftCircleCenterX;
    private float mSecArcRightCircleCenterX;
    private int mSecTrackTopY;
    private float mThrArcAngleDegree;
    private float mThrArcCircleCenterY;
    private float mThrArcCircleRadius;
    private float mThrArcLeftCircleCenterX;
    private float mThrArcRightCircleCenterX;
    private int mThrTrackTopY;
    private int mTrackLeftX;
    private int mTrackRightX;
    private int mTrackTopY;
    private int mViewHeight;
    private int mViewWidth;
    private WindowManager mWindowManager;

    private void updateSizeForScreenSizeChange() {
        Point point = this.mScreenSize;
        int min = Math.min(point.x, point.y);
        int i = this.mScreenSize.y;
        float f = (((float) min) * 1.0f) / 1080.0f;
        int i2 = (int) (6.0f * f);
        this.mOuterCircleWidth = i2;
        int i3 = (int) (4.0f * f);
        this.mOuterSecCircleWidth = i3;
        this.mOuterThrCircleWidth = i3;
        int i4 = (int) (378.0f * f);
        this.mOuterCircleRadius = i4;
        this.mOuterSecCircleRadius = (int) (358.0f * f);
        this.mOuterThrCircleRadius = (int) (338.0f * f);
        int i5 = i2 + i4;
        this.mOuterCircleCenterX = i5;
        this.mOuterCircleCenterY = i5;
        this.mViewWidth = (i2 + i4) * 2;
        this.mViewHeight = (i / 2) + i5;
        int i6 = (int) (475.0f * f);
        int i7 = (int) (455.0f * f);
        int i8 = (int) (435.0f * f);
        int i9 = (int) (f * 122.0f);
        int i10 = i9 / 2;
        this.mTrackLeftX = i5 - i10;
        this.mTrackRightX = i10 + i5;
        this.mTrackTopY = i5 + i6;
        this.mSecTrackTopY = i5 + i7;
        this.mThrTrackTopY = i5 + i8;
        float f2 = (float) i6;
        float f3 = ((float) i9) / 2.0f;
        float f4 = (float) i4;
        float f5 = f3 * f3;
        float f6 = (((f2 * f2) + f5) - (f4 * f4)) / ((f4 - f3) * 2.0f);
        this.mArcCircleRadius = f6;
        this.mArcAngleDegree = (float) (((double) (((float) Math.atan((double) ((f6 + f3) / f2))) * 180.0f)) / 3.141592653589793d);
        float f7 = this.mArcCircleRadius;
        this.mArcLeftCircleCenterX = ((float) this.mTrackLeftX) - f7;
        this.mArcRightCircleCenterX = ((float) this.mTrackRightX) + f7;
        this.mArcCircleCenterY = (float) this.mTrackTopY;
        float f8 = (float) i7;
        float f9 = (float) this.mOuterSecCircleRadius;
        float f10 = (((f8 * f8) + f5) - (f9 * f9)) / ((f9 - f3) * 2.0f);
        this.mSecArcCircleRadius = f10;
        this.mSecArcAngleDegree = (float) (((double) (((float) Math.atan((double) ((f10 + f3) / f8))) * 180.0f)) / 3.141592653589793d);
        float f11 = this.mSecArcCircleRadius;
        this.mSecArcLeftCircleCenterX = ((float) this.mTrackLeftX) - f11;
        this.mSecArcRightCircleCenterX = ((float) this.mTrackRightX) + f11;
        this.mSecArcCircleCenterY = (float) this.mSecTrackTopY;
        float f12 = (float) i8;
        float f13 = (float) this.mOuterThrCircleRadius;
        float f14 = (((f12 * f12) + f5) - (f13 * f13)) / ((f13 - f3) * 2.0f);
        this.mThrArcCircleRadius = f14;
        this.mThrArcAngleDegree = (float) (((double) (((float) Math.atan((double) ((f14 + f3) / f12))) * 180.0f)) / 3.141592653589793d);
        float f15 = this.mThrArcCircleRadius;
        this.mThrArcLeftCircleCenterX = ((float) this.mTrackLeftX) - f15;
        this.mThrArcRightCircleCenterX = ((float) this.mTrackRightX) + f15;
        this.mThrArcCircleCenterY = (float) this.mThrTrackTopY;
        Log.i("OutlineView", "updateSizeForScreenSizeChange:  screenWidth: " + min + " screenHeight: " + i + " IS_NOTCH " + Constants.IS_NOTCH);
    }

    public OutlineView(Context context) {
        this(context, null);
    }

    public OutlineView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public OutlineView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(context);
    }

    private void init(Context context) {
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mScreenSize = new Point();
        this.mWindowManager.getDefaultDisplay().getRealSize(this.mScreenSize);
        updateSizeForScreenSizeChange();
        Paint paint = new Paint(1);
        this.mOutCirclePaint = paint;
        paint.setStyle(Paint.Style.STROKE);
        this.mOutCirclePaint.setStrokeWidth((float) this.mOuterCircleWidth);
        LinearGradient linearGradient = new LinearGradient(0.0f, 0.0f, 0.0f, (float) this.mViewHeight, new int[]{OUTER_CIRCLE_START_COLOR, OUTER_CIRCLE_MIDDLE_COLOR, OUTER_CIRCLE_END_COLOR}, new float[]{0.0f, 0.34f, 1.0f}, Shader.TileMode.CLAMP);
        this.mOutCirclePaint.setShader(linearGradient);
        Paint paint2 = new Paint(1);
        this.mOutSecCirclePaint = paint2;
        paint2.setStyle(Paint.Style.STROKE);
        this.mOutSecCirclePaint.setStrokeWidth((float) this.mOuterSecCircleWidth);
        this.mOutSecCirclePaint.setAlpha(178);
        this.mOutSecCirclePaint.setShader(linearGradient);
        Paint paint3 = new Paint(1);
        this.mOutThrCirclePaint = paint3;
        paint3.setStyle(Paint.Style.STROKE);
        this.mOutThrCirclePaint.setStrokeWidth((float) this.mOuterThrCircleWidth);
        this.mOutThrCirclePaint.setAlpha(R$styleable.Constraint_layout_goneMarginStart);
        this.mOutThrCirclePaint.setShader(linearGradient);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int i = this.mOuterCircleCenterX;
        int i2 = this.mOuterCircleRadius;
        int i3 = this.mOuterCircleCenterY;
        float f = this.mArcAngleDegree;
        canvas.drawArc((float) (i - i2), (float) (i3 - i2), (float) (i + i2), (float) (i3 + i2), f - 270.0f, 360.0f - (f * 2.0f), false, this.mOutCirclePaint);
        int i4 = this.mOuterCircleCenterX;
        int i5 = this.mOuterSecCircleRadius;
        int i6 = this.mOuterCircleCenterY;
        float f2 = this.mSecArcAngleDegree;
        canvas.drawArc((float) (i4 - i5), (float) (i6 - i5), (float) (i4 + i5), (float) (i6 + i5), f2 - 270.0f, 360.0f - (f2 * 2.0f), false, this.mOutSecCirclePaint);
        int i7 = this.mOuterCircleCenterX;
        int i8 = this.mOuterThrCircleRadius;
        int i9 = this.mOuterCircleCenterY;
        float f3 = this.mThrArcAngleDegree;
        canvas.drawArc((float) (i7 - i8), (float) (i9 - i8), (float) (i7 + i8), (float) (i9 + i8), f3 - 270.0f, 360.0f - (f3 * 2.0f), false, this.mOutThrCirclePaint);
        float f4 = this.mArcLeftCircleCenterX;
        float f5 = this.mArcCircleRadius;
        float f6 = this.mArcCircleCenterY;
        float f7 = this.mArcAngleDegree;
        canvas.drawArc(f4 - f5, f6 - f5, f4 + f5, f6 + f5, f7 - 90.0f, 90.0f - f7, false, this.mOutCirclePaint);
        float f8 = this.mSecArcLeftCircleCenterX;
        float f9 = this.mSecArcCircleRadius;
        float f10 = this.mSecArcCircleCenterY;
        float f11 = this.mSecArcAngleDegree;
        canvas.drawArc(f8 - f9, f10 - f9, f8 + f9, f10 + f9, f11 - 90.0f, 90.0f - f11, false, this.mOutSecCirclePaint);
        float f12 = this.mThrArcLeftCircleCenterX;
        float f13 = this.mThrArcCircleRadius;
        float f14 = this.mThrArcCircleCenterY;
        float f15 = this.mThrArcAngleDegree;
        canvas.drawArc(f12 - f13, f14 - f13, f12 + f13, f14 + f13, f15 - 90.0f, 90.0f - f15, false, this.mOutThrCirclePaint);
        float f16 = this.mArcRightCircleCenterX;
        float f17 = this.mArcCircleRadius;
        float f18 = this.mArcCircleCenterY;
        canvas.drawArc(f16 - f17, f18 - f17, f16 + f17, f18 + f17, 180.0f, 90.0f - this.mArcAngleDegree, false, this.mOutCirclePaint);
        float f19 = this.mSecArcRightCircleCenterX;
        float f20 = this.mSecArcCircleRadius;
        float f21 = this.mSecArcCircleCenterY;
        canvas.drawArc(f19 - f20, f21 - f20, f19 + f20, f21 + f20, 180.0f, 90.0f - this.mSecArcAngleDegree, false, this.mOutSecCirclePaint);
        float f22 = this.mThrArcRightCircleCenterX;
        float f23 = this.mThrArcCircleRadius;
        float f24 = this.mThrArcCircleCenterY;
        canvas.drawArc(f22 - f23, f24 - f23, f22 + f23, f24 + f23, 180.0f, 90.0f - this.mThrArcAngleDegree, false, this.mOutThrCirclePaint);
        int i10 = this.mTrackLeftX;
        canvas.drawLine((float) i10, (float) this.mTrackTopY, (float) i10, (float) this.mViewHeight, this.mOutCirclePaint);
        int i11 = this.mTrackRightX;
        canvas.drawLine((float) i11, (float) this.mTrackTopY, (float) i11, (float) this.mViewHeight, this.mOutCirclePaint);
        int i12 = this.mTrackLeftX;
        canvas.drawLine((float) i12, (float) this.mSecTrackTopY, (float) i12, (float) this.mViewHeight, this.mOutSecCirclePaint);
        int i13 = this.mTrackRightX;
        canvas.drawLine((float) i13, (float) this.mSecTrackTopY, (float) i13, (float) this.mViewHeight, this.mOutSecCirclePaint);
        int i14 = this.mTrackLeftX;
        canvas.drawLine((float) i14, (float) this.mThrTrackTopY, (float) i14, (float) this.mViewHeight, this.mOutThrCirclePaint);
        int i15 = this.mTrackRightX;
        canvas.drawLine((float) i15, (float) this.mThrTrackTopY, (float) i15, (float) this.mViewHeight, this.mOutThrCirclePaint);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        setMeasuredDimension(this.mViewWidth, this.mViewHeight);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        checkScreenSize();
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        checkScreenSize();
    }

    private void checkScreenSize() {
        Point point = new Point();
        this.mWindowManager.getDefaultDisplay().getRealSize(point);
        if (!this.mScreenSize.equals(point.x, point.y)) {
            this.mScreenSize.set(point.x, point.y);
            updateSizeForScreenSizeChange();
            this.mOutCirclePaint.setStrokeWidth((float) this.mOuterCircleWidth);
            this.mOutSecCirclePaint.setStrokeWidth((float) this.mOuterSecCircleWidth);
            this.mOutThrCirclePaint.setStrokeWidth((float) this.mOuterThrCircleWidth);
            LinearGradient linearGradient = new LinearGradient(0.0f, 0.0f, 0.0f, (float) this.mViewHeight, new int[]{OUTER_CIRCLE_START_COLOR, OUTER_CIRCLE_MIDDLE_COLOR, OUTER_CIRCLE_END_COLOR}, new float[]{0.0f, 0.34f, 1.0f}, Shader.TileMode.CLAMP);
            this.mOutCirclePaint.setShader(linearGradient);
            this.mOutSecCirclePaint.setShader(linearGradient);
            this.mOutThrCirclePaint.setShader(linearGradient);
            requestLayout();
        }
    }
}
