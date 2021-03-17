package com.android.systemui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import com.android.settingslib.Utils;

public class HardwareBgDrawable extends LayerDrawable {
    private final Drawable[] mLayers;
    private final Paint mPaint;
    private int mPoint;
    private boolean mRotatedBackground;
    private final boolean mRoundTop;

    public int getOpacity() {
        return -1;
    }

    public HardwareBgDrawable(boolean z, boolean z2, Context context) {
        this(z, getLayers(context, z, z2));
    }

    public HardwareBgDrawable(boolean z, Drawable[] drawableArr) {
        super(drawableArr);
        this.mPaint = new Paint();
        if (drawableArr.length == 2) {
            this.mRoundTop = z;
            this.mLayers = drawableArr;
            return;
        }
        throw new IllegalArgumentException("Need 2 layers");
    }

    private static Drawable[] getLayers(Context context, boolean z, boolean z2) {
        Drawable[] drawableArr;
        int i;
        int i2 = z2 ? C0013R$drawable.rounded_bg_full : C0013R$drawable.rounded_bg;
        if (z) {
            drawableArr = new Drawable[]{context.getDrawable(i2).mutate(), context.getDrawable(i2).mutate()};
        } else {
            drawableArr = new Drawable[2];
            drawableArr[0] = context.getDrawable(i2).mutate();
            if (z2) {
                i = C0013R$drawable.rounded_full_bg_bottom;
            } else {
                i = C0013R$drawable.rounded_bg_bottom;
            }
            drawableArr[1] = context.getDrawable(i).mutate();
        }
        drawableArr[1].setTintList(Utils.getColorAttr(context, 16843827));
        return drawableArr;
    }

    public void draw(Canvas canvas) {
        if (this.mPoint < 0 || this.mRotatedBackground) {
            this.mLayers[0].draw(canvas);
            return;
        }
        Rect bounds = getBounds();
        int i = bounds.top + this.mPoint;
        int i2 = bounds.bottom;
        if (i > i2) {
            i = i2;
        }
        if (this.mRoundTop) {
            this.mLayers[0].setBounds(bounds.left, bounds.top, bounds.right, i);
        } else {
            this.mLayers[1].setBounds(bounds.left, i, bounds.right, bounds.bottom);
        }
        if (this.mRoundTop) {
            this.mLayers[1].draw(canvas);
            this.mLayers[0].draw(canvas);
            return;
        }
        this.mLayers[0].draw(canvas);
        this.mLayers[1].draw(canvas);
    }

    public void setAlpha(int i) {
        this.mPaint.setAlpha(i);
    }

    public void setColorFilter(ColorFilter colorFilter) {
        this.mPaint.setColorFilter(colorFilter);
    }
}
