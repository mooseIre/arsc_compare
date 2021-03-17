package com.android.systemui.statusbar.notification;

import android.content.Context;
import android.graphics.Color;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import com.android.internal.util.ContrastColorUtil;
import com.android.systemui.C0015R$id;

public class NotificationUtils {
    private static final int[] sLocationBase = new int[2];
    private static final int[] sLocationOffset = new int[2];
    private static Boolean sUseNewInterruptionModel;

    public static float interpolate(float f, float f2, float f3) {
        return (f * (1.0f - f3)) + (f2 * f3);
    }

    public static boolean isGrayscale(ImageView imageView, ContrastColorUtil contrastColorUtil) {
        Object tag = imageView.getTag(C0015R$id.icon_is_grayscale);
        if (tag != null) {
            return Boolean.TRUE.equals(tag);
        }
        boolean isGrayscaleIcon = contrastColorUtil.isGrayscaleIcon(imageView.getDrawable());
        imageView.setTag(C0015R$id.icon_is_grayscale, Boolean.valueOf(isGrayscaleIcon));
        return isGrayscaleIcon;
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

    public static int getFontScaledHeight(Context context, int i) {
        return (int) (((float) context.getResources().getDimensionPixelSize(i)) * Math.max(1.0f, context.getResources().getDisplayMetrics().scaledDensity / context.getResources().getDisplayMetrics().density));
    }

    public static boolean useNewInterruptionModel(Context context) {
        if (sUseNewInterruptionModel == null) {
            boolean z = true;
            if (Settings.Secure.getInt(context.getContentResolver(), "new_interruption_model", 1) == 0) {
                z = false;
            }
            sUseNewInterruptionModel = Boolean.valueOf(z);
        }
        return sUseNewInterruptionModel.booleanValue();
    }
}
