package com.android.systemui.statusbar.notification;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.View;
import android.widget.ImageView;
import com.android.systemui.plugins.R;

public class NotificationUtils {
    private static final int[] sLocationBase = new int[2];
    private static final int[] sLocationOffset = new int[2];

    public static float interpolate(float f, float f2, float f3) {
        return (f * (1.0f - f3)) + (f2 * f3);
    }

    public static boolean isGrayscale(ImageView imageView, CompatibilityColorUtil compatibilityColorUtil) {
        Object tag = imageView.getTag(R.id.icon_is_grayscale);
        if (tag != null) {
            return Boolean.TRUE.equals(tag);
        }
        boolean isGrayscaleIcon = isGrayscaleIcon(imageView.getDrawable(), compatibilityColorUtil);
        imageView.setTag(R.id.icon_is_grayscale, Boolean.valueOf(isGrayscaleIcon));
        return isGrayscaleIcon;
    }

    private static boolean isGrayscaleIcon(Drawable drawable, CompatibilityColorUtil compatibilityColorUtil) {
        if (!(drawable instanceof LayerDrawable)) {
            return compatibilityColorUtil.isGrayscaleIcon(drawable);
        }
        LayerDrawable layerDrawable = (LayerDrawable) drawable;
        for (int i = 0; i < layerDrawable.getNumberOfLayers(); i++) {
            if (!isGrayscaleIcon(layerDrawable.getDrawable(i), compatibilityColorUtil)) {
                return false;
            }
        }
        return true;
    }

    public static int interpolateColors(int i, int i2, float f) {
        return Color.argb((int) interpolate((float) Color.alpha(i), (float) Color.alpha(i2), f), (int) interpolate((float) Color.red(i), (float) Color.red(i2), f), (int) interpolate((float) Color.green(i), (float) Color.green(i2), f), (int) interpolate((float) Color.blue(i), (float) Color.blue(i2), f));
    }

    public static float getRelativeYOffset(View view, View view2) {
        int[] iArr = sLocationOffset;
        int[] iArr2 = sLocationBase;
        view2.getLocationOnScreen(iArr2);
        view.getLocationOnScreen(iArr);
        return (float) (iArr[1] - iArr2[1]);
    }
}
