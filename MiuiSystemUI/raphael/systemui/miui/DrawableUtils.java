package com.android.systemui.miui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RotateDrawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Build;

public class DrawableUtils {
    public static Drawable findDrawableById(Drawable drawable, int i) {
        if (drawable == null) {
            return null;
        }
        if (drawable instanceof LayerDrawable) {
            LayerDrawable layerDrawable = (LayerDrawable) drawable;
            for (int i2 = 0; i2 < layerDrawable.getNumberOfLayers(); i2++) {
                if (layerDrawable.getId(i2) == i) {
                    return layerDrawable.getDrawable(i2);
                }
                Drawable findDrawableById = findDrawableById(layerDrawable.getDrawable(i2), i);
                if (findDrawableById != null) {
                    return findDrawableById;
                }
            }
        } else if (isWrapperDrawable(drawable)) {
            return findDrawableById(getWrappedDrawable(drawable, (Drawable) null), i);
        }
        return null;
    }

    private static boolean isWrapperDrawable(Drawable drawable) {
        return (Build.VERSION.SDK_INT >= 23 && (drawable instanceof DrawableWrapper)) || (drawable instanceof ScaleDrawable) || (drawable instanceof ClipDrawable) || (drawable instanceof InsetDrawable) || (drawable instanceof RotateDrawable);
    }

    private static Drawable getWrappedDrawable(Drawable drawable, Drawable drawable2) {
        if (Build.VERSION.SDK_INT >= 23 && (drawable instanceof DrawableWrapper)) {
            return ((DrawableWrapper) drawable).getDrawable();
        }
        if (drawable instanceof ScaleDrawable) {
            return ((ScaleDrawable) drawable).getDrawable();
        }
        if (drawable instanceof ClipDrawable) {
            return ((ClipDrawable) drawable).getDrawable();
        }
        if (drawable instanceof InsetDrawable) {
            return ((InsetDrawable) drawable).getDrawable();
        }
        return drawable instanceof RotateDrawable ? ((RotateDrawable) drawable).getDrawable() : drawable2;
    }

    public static LayerDrawable combine(Drawable drawable, Drawable drawable2, int i) {
        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{drawable, drawable2});
        layerDrawable.setLayerGravity(1, i);
        return layerDrawable;
    }

    public static Bitmap drawable2Bitmap(Drawable drawable) {
        Bitmap.Config config;
        if (drawable == null) {
            return null;
        }
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();
        if (drawable.getOpacity() != -1) {
            config = Bitmap.Config.ARGB_8888;
        } else {
            config = Bitmap.Config.RGB_565;
        }
        Bitmap createBitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, config);
        Canvas canvas = new Canvas(createBitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return createBitmap;
    }
}
