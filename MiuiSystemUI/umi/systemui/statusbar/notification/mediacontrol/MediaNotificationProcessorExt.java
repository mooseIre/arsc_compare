package com.android.systemui.statusbar.notification.mediacontrol;

import com.android.internal.util.ContrastColorUtil;
import com.android.systemui.statusbar.notification.mediacontrol.ProcessArtworkTask;

public class MediaNotificationProcessorExt {
    public static ProcessArtworkTask.Result recalculateColors(ProcessArtworkTask.Result result) {
        if (result == null) {
            return null;
        }
        int i = result.backgroundColor;
        int i2 = result.foregroundColor;
        double calculateLuminance = ContrastColorUtil.calculateLuminance(i);
        double calculateLuminance2 = ContrastColorUtil.calculateLuminance(i2);
        double calculateContrast = ContrastColorUtil.calculateContrast(i2, i);
        boolean z = (calculateLuminance > calculateLuminance2 && ContrastColorUtil.satisfiesTextContrast(i, -16777216)) || (calculateLuminance <= calculateLuminance2 && !ContrastColorUtil.satisfiesTextContrast(i, -1));
        int i3 = (calculateContrast > 4.5d ? 1 : (calculateContrast == 4.5d ? 0 : -1));
        int i4 = -20;
        if (i3 >= 0) {
            result.primaryTextColor = i2;
            int changeColorLightness = ContrastColorUtil.changeColorLightness(i2, z ? 20 : -10);
            result.secondaryTextColor = changeColorLightness;
            if (ContrastColorUtil.calculateContrast(changeColorLightness, i) < 4.5d) {
                if (z) {
                    result.secondaryTextColor = ContrastColorUtil.findContrastColor(result.secondaryTextColor, i, true, 4.5d);
                } else {
                    result.secondaryTextColor = ContrastColorUtil.findContrastColorAgainstDark(result.secondaryTextColor, i, true, 4.5d);
                }
                int i5 = result.secondaryTextColor;
                if (!z) {
                    i4 = 10;
                }
                result.primaryTextColor = ContrastColorUtil.changeColorLightness(i5, i4);
            }
        } else if (z) {
            int findContrastColor = ContrastColorUtil.findContrastColor(i2, i, true, 4.5d);
            result.secondaryTextColor = findContrastColor;
            result.primaryTextColor = ContrastColorUtil.changeColorLightness(findContrastColor, -20);
        } else {
            int findContrastColorAgainstDark = ContrastColorUtil.findContrastColorAgainstDark(i2, i, true, 4.5d);
            result.secondaryTextColor = findContrastColorAgainstDark;
            result.primaryTextColor = ContrastColorUtil.changeColorLightness(findContrastColorAgainstDark, 10);
        }
        return result;
    }
}
