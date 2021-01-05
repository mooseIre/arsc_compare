package com.android.systemui.statusbar.notification.mediacontrol;

import androidx.core.graphics.ColorUtils;
import androidx.palette.graphics.Palette;

public class MediaNotificationProcessorExt {
    public static int computeBackgroundColor(Palette.Swatch swatch) {
        float[] fArr = {0.0f, 0.0f, 0.0f};
        ColorUtils.colorToHSL(swatch != null ? swatch.getRgb() : -1, fArr);
        float f = fArr[2];
        if (f < 0.05f || f > 0.95f) {
            fArr[1] = 0.0f;
        }
        fArr[1] = fArr[1] * 0.8f;
        fArr[2] = 0.25f;
        return ColorUtils.HSLToColor(fArr);
    }
}
