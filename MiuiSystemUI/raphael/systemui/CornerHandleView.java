package com.android.systemui;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.View;
import com.android.settingslib.Utils;

public class CornerHandleView extends View {
    private static int mDisableRoundedCorner = SystemProperties.getInt("vendor.display.disable_rounded_corner", 0);
    private int mDarkColor;
    private int mLightColor;
    private Paint mPaint;
    private Path mPath;
    private boolean mRequiresInvalidate;

    public CornerHandleView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        Paint paint = new Paint();
        this.mPaint = paint;
        paint.setAntiAlias(true);
        this.mPaint.setStyle(Paint.Style.STROKE);
        this.mPaint.setStrokeCap(Paint.Cap.ROUND);
        this.mPaint.setStrokeWidth(getStrokePx());
        int themeAttr = Utils.getThemeAttr(((View) this).mContext, C0009R$attr.darkIconTheme);
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(((View) this).mContext, Utils.getThemeAttr(((View) this).mContext, C0009R$attr.lightIconTheme));
        ContextThemeWrapper contextThemeWrapper2 = new ContextThemeWrapper(((View) this).mContext, themeAttr);
        this.mLightColor = Utils.getColorAttrDefaultColor(contextThemeWrapper, C0009R$attr.singleToneColor);
        this.mDarkColor = Utils.getColorAttrDefaultColor(contextThemeWrapper2, C0009R$attr.singleToneColor);
        updatePath();
    }

    public void setAlpha(float f) {
        super.setAlpha(f);
        if (f > 0.0f && this.mRequiresInvalidate) {
            this.mRequiresInvalidate = false;
            invalidate();
        }
    }

    private void updatePath() {
        this.mPath = new Path();
        float marginPx = getMarginPx();
        float innerRadiusPx = getInnerRadiusPx();
        float strokePx = getStrokePx() / 2.0f;
        float angle = getAngle();
        float f = ((90.0f - angle) / 2.0f) + 180.0f;
        float f2 = marginPx + strokePx;
        float f3 = innerRadiusPx * 2.0f;
        float f4 = (marginPx + f3) - strokePx;
        RectF rectF = new RectF(f2, f2, f4, f4);
        if (angle >= 90.0f) {
            float f5 = marginPx + innerRadiusPx;
            float convertDpToPixel = convertDpToPixel(((31.0f - ((convertPixelToDp(f3 * 3.1415927f, ((View) this).mContext) * getAngle()) / 360.0f)) - 8.0f) / 2.0f, ((View) this).mContext) + f5;
            this.mPath.moveTo(f2, convertDpToPixel);
            this.mPath.lineTo(f2, f5);
            this.mPath.arcTo(rectF, f, angle);
            this.mPath.moveTo(f5, f2);
            this.mPath.lineTo(convertDpToPixel, f2);
            return;
        }
        this.mPath.arcTo(rectF, f, angle);
    }

    public void updateDarkness(float f) {
        int intValue = ((Integer) ArgbEvaluator.getInstance().evaluate(f, Integer.valueOf(this.mLightColor), Integer.valueOf(this.mDarkColor))).intValue();
        if (this.mPaint.getColor() != intValue) {
            this.mPaint.setColor(intValue);
            if (getVisibility() != 0 || getAlpha() <= 0.0f) {
                this.mRequiresInvalidate = true;
            } else {
                invalidate();
            }
        }
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(this.mPath, this.mPaint);
    }

    private static float convertDpToPixel(float f, Context context) {
        return f * (((float) context.getResources().getDisplayMetrics().densityDpi) / 160.0f);
    }

    private static float convertPixelToDp(float f, Context context) {
        return (f * 160.0f) / ((float) context.getResources().getDisplayMetrics().densityDpi);
    }

    private float getAngle() {
        float convertPixelToDp = (31.0f / convertPixelToDp((getOuterRadiusPx() * 2.0f) * 3.1415927f, ((View) this).mContext)) * 360.0f;
        if (convertPixelToDp > 90.0f) {
            return 90.0f;
        }
        return convertPixelToDp;
    }

    private float getMarginPx() {
        return convertDpToPixel(8.0f, ((View) this).mContext);
    }

    private float getInnerRadiusPx() {
        return getOuterRadiusPx() - getMarginPx();
    }

    private float getOuterRadiusPx() {
        int dimensionPixelSize = getResources().getDimensionPixelSize(C0012R$dimen.config_rounded_mask_size_bottom);
        if (dimensionPixelSize == 0 && mDisableRoundedCorner == 0) {
            dimensionPixelSize = getResources().getDimensionPixelSize(C0012R$dimen.config_rounded_mask_size);
        }
        if (dimensionPixelSize == 0) {
            dimensionPixelSize = getResources().getDimensionPixelSize(C0012R$dimen.config_rounded_mask_size_top);
        }
        if (dimensionPixelSize == 0) {
            dimensionPixelSize = (int) convertDpToPixel(15.0f, ((View) this).mContext);
        }
        return (float) dimensionPixelSize;
    }

    private float getStrokePx() {
        return convertDpToPixel(getAngle() < 90.0f ? 2.0f : 1.95f, getContext());
    }
}
