package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.view.View;
import com.android.systemui.C0011R$color;

public class KeyguardUserSwitcherScrim extends Drawable implements View.OnLayoutChangeListener {
    private int mAlpha = 255;
    private int mDarkColor;
    private int mLayoutWidth;
    private Paint mRadialGradientPaint = new Paint();
    private int mTop;

    public int getOpacity() {
        return -3;
    }

    public void setColorFilter(ColorFilter colorFilter) {
    }

    public KeyguardUserSwitcherScrim(Context context) {
        this.mDarkColor = context.getColor(C0011R$color.keyguard_user_switcher_background_gradient_color);
    }

    public void draw(Canvas canvas) {
        boolean z = getLayoutDirection() == 0;
        Rect bounds = getBounds();
        float width = ((float) bounds.width()) * 2.5f;
        canvas.translate(0.0f, (float) (-this.mTop));
        canvas.scale(1.0f, (((float) (this.mTop + bounds.height())) * 2.5f) / width);
        canvas.drawRect(z ? ((float) bounds.right) - width : 0.0f, 0.0f, z ? (float) bounds.right : ((float) bounds.left) + width, width, this.mRadialGradientPaint);
    }

    public void setAlpha(int i) {
        this.mAlpha = i;
        updatePaint();
        invalidateSelf();
    }

    public int getAlpha() {
        return this.mAlpha;
    }

    public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        if (i != i5 || i2 != i6 || i3 != i7 || i4 != i8) {
            this.mLayoutWidth = i3 - i;
            this.mTop = i2;
            updatePaint();
        }
    }

    private void updatePaint() {
        int i = this.mLayoutWidth;
        if (i != 0) {
            float f = ((float) i) * 2.5f;
            this.mRadialGradientPaint.setShader(new RadialGradient(getLayoutDirection() == 0 ? (float) this.mLayoutWidth : 0.0f, 0.0f, f, new int[]{Color.argb((int) (((float) (Color.alpha(this.mDarkColor) * this.mAlpha)) / 255.0f), 0, 0, 0), 0}, new float[]{Math.max(0.0f, (((float) this.mLayoutWidth) * 0.75f) / f), 1.0f}, Shader.TileMode.CLAMP));
        }
    }
}
