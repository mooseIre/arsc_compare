package com.android.systemui;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class SmoothRoundDrawable extends Drawable {
    private Rect mBounds = new Rect();
    private int mColor;
    private Paint mPaint = new Paint(1);
    private Path mPath = null;
    private SmoothPathProvider mPathProvider = new SmoothPathProvider();
    private float[] mRadii;
    private float mRadius;
    private float mSmooth;

    public int getOpacity() {
        return -3;
    }

    public void inflate(Resources resources, XmlPullParser xmlPullParser, AttributeSet attributeSet, Resources.Theme theme) throws IOException, XmlPullParserException {
        super.inflate(resources, xmlPullParser, attributeSet, theme);
        TypedArray obtainAttributes = Drawable.obtainAttributes(resources, theme, attributeSet, R$styleable.SmoothRoundDrawable);
        this.mRadius = (float) obtainAttributes.getDimensionPixelSize(1, 0);
        this.mColor = obtainAttributes.getInt(0, 0);
        if (obtainAttributes.hasValue(2) || obtainAttributes.hasValue(3) || obtainAttributes.hasValue(5) || obtainAttributes.hasValue(4)) {
            float dimensionPixelSize = (float) obtainAttributes.getDimensionPixelSize(2, 0);
            float dimensionPixelSize2 = (float) obtainAttributes.getDimensionPixelSize(3, 0);
            float dimensionPixelSize3 = (float) obtainAttributes.getDimensionPixelSize(5, 0);
            float dimensionPixelSize4 = (float) obtainAttributes.getDimensionPixelSize(4, 0);
            this.mRadii = new float[]{dimensionPixelSize, dimensionPixelSize, dimensionPixelSize2, dimensionPixelSize2, dimensionPixelSize3, dimensionPixelSize3, dimensionPixelSize4, dimensionPixelSize4};
        }
        this.mSmooth = 0.7f;
        obtainAttributes.recycle();
        this.mPaint.setColor(this.mColor);
        this.mPaint.setStyle(Paint.Style.FILL);
    }

    /* access modifiers changed from: protected */
    public void onBoundsChange(Rect rect) {
        if (!this.mBounds.equals(rect)) {
            this.mPath = null;
        }
    }

    public void draw(Canvas canvas) {
        if (this.mPath == null) {
            this.mPath = getSmoothPathFromProvider(getBounds(), this.mSmooth);
        }
        canvas.drawPath(this.mPath, this.mPaint);
    }

    public void setAlpha(int i) {
        this.mPaint.setAlpha(i);
    }

    public void setColorFilter(ColorFilter colorFilter) {
        this.mPaint.setColorFilter(colorFilter);
    }

    private Path getSmoothPathFromProvider(Rect rect, float f) {
        if (this.mRadii == null) {
            this.mPathProvider.buildSmoothData(rect.width(), rect.height(), this.mRadius, (double) f);
        } else {
            this.mPathProvider.buildSmoothData(rect.width(), rect.height(), this.mRadii, (double) f);
        }
        return this.mPathProvider.getSmoothPath();
    }
}
