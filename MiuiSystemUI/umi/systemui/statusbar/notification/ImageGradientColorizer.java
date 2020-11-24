package com.android.systemui.statusbar.notification;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

public class ImageGradientColorizer {
    public Bitmap colorize(Drawable drawable, int i, boolean z) {
        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();
        int min = Math.min(intrinsicWidth, intrinsicHeight);
        int i2 = (intrinsicWidth - min) / 2;
        int i3 = (intrinsicHeight - min) / 2;
        Drawable mutate = drawable.mutate();
        mutate.setBounds(-i2, -i3, intrinsicWidth - i2, intrinsicHeight - i3);
        Bitmap createBitmap = Bitmap.createBitmap(min, min, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        int red = Color.red(i);
        float f = (float) red;
        float green = (float) Color.green(i);
        float blue = (float) Color.blue(i);
        float f2 = (((f / 255.0f) * 0.2126f) + ((green / 255.0f) * 0.7152f) + ((blue / 255.0f) * 0.0722f)) * 255.0f;
        ColorMatrix colorMatrix = new ColorMatrix(new float[]{0.2126f, 0.7152f, 0.0722f, 0.0f, f - f2, 0.2126f, 0.7152f, 0.0722f, 0.0f, green - f2, 0.2126f, 0.7152f, 0.0722f, 0.0f, blue - f2, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f});
        Paint paint = new Paint(1);
        float f3 = (float) min;
        paint.setShader(new LinearGradient(0.0f, 0.0f, f3, 0.0f, new int[]{0, Color.argb(0.5f, 1.0f, 1.0f, 1.0f), -16777216}, new float[]{0.0f, 0.4f, 1.0f}, Shader.TileMode.CLAMP));
        Bitmap createBitmap2 = Bitmap.createBitmap(min, min, Bitmap.Config.ARGB_8888);
        Canvas canvas2 = new Canvas(createBitmap2);
        mutate.clearColorFilter();
        mutate.draw(canvas2);
        if (z) {
            canvas2.translate(f3, 0.0f);
            canvas2.scale(-1.0f, 1.0f);
        }
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas2.drawPaint(paint);
        Paint paint2 = new Paint(1);
        paint2.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        paint2.setAlpha(127);
        canvas.drawBitmap(createBitmap2, 0.0f, 0.0f, paint2);
        paint.setShader(new LinearGradient(0.0f, 0.0f, f3, 0.0f, new int[]{0, Color.argb(0.5f, 1.0f, 1.0f, 1.0f), -16777216}, new float[]{0.0f, 0.6f, 1.0f}, Shader.TileMode.CLAMP));
        canvas2.drawPaint(paint);
        canvas.drawBitmap(createBitmap2, 0.0f, 0.0f, (Paint) null);
        return createBitmap;
    }
}
