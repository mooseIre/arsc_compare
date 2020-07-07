package com.android.systemui.miui.widget;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

public class CenterTextDrawable extends Drawable {
    private Paint mPaint = new Paint(1);
    private String mText = "";

    public int getOpacity() {
        return -3;
    }

    public CenterTextDrawable() {
        this.mPaint.setTextAlign(Paint.Align.CENTER);
        this.mPaint.setFakeBoldText(false);
    }

    public void setText(String str) {
        this.mText = str;
        invalidateSelf();
    }

    public void draw(Canvas canvas) {
        if (!TextUtils.isEmpty(this.mText)) {
            canvas.drawText(this.mText, (float) (getBounds().width() / 2), (float) ((int) (((float) (getBounds().height() / 2)) - ((this.mPaint.descent() + this.mPaint.ascent()) / 2.0f))), this.mPaint);
        }
    }

    public void setAlpha(int i) {
        this.mPaint.setAlpha(i);
    }

    public int getAlpha() {
        return this.mPaint.getAlpha();
    }

    public void setTextColor(int i) {
        this.mPaint.setColor(i);
    }

    public void setTextSize(float f) {
        this.mPaint.setTextSize(f);
    }

    public void setColorFilter(ColorFilter colorFilter) {
        this.mPaint.setColorFilter(colorFilter);
    }
}
