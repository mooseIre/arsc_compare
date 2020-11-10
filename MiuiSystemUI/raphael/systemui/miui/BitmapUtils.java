package com.android.systemui.miui;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import miui.graphics.BitmapFactory;
import miui.system.R;

public class BitmapUtils {
    public static Bitmap view2Bitmap(View view, int i, int i2) {
        if (i <= 0 || i2 <= 0) {
            return null;
        }
        Bitmap createBitmap = Bitmap.createBitmap(i, i2, Bitmap.Config.ARGB_8888);
        view.draw(new Canvas(createBitmap));
        return createBitmap;
    }

    public static Bitmap getBlurBackground(Bitmap bitmap, Bitmap bitmap2) {
        if (bitmap != null) {
            bitmap2 = BitmapFactory.fastBlur(bitmap, bitmap2, Resources.getSystem().getDimensionPixelSize(285606022));
        }
        if (bitmap2 != null) {
            new Canvas(bitmap2).drawColor(Resources.getSystem().getColor(R.color.blur_background_mask));
        }
        return bitmap2;
    }
}
