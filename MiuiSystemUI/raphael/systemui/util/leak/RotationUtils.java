package com.android.systemui.util.leak;

import android.content.Context;
import android.content.res.Configuration;

public class RotationUtils {
    public static int getRotation(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        int rotation = context.getDisplay().getRotation();
        if (configuration.smallestScreenWidthDp >= 600) {
            return 0;
        }
        if (rotation == 1) {
            return 1;
        }
        return rotation == 3 ? 2 : 0;
    }

    public static int getExactRotation(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        int rotation = context.getDisplay().getRotation();
        if (configuration.smallestScreenWidthDp >= 600) {
            return 0;
        }
        if (rotation == 1) {
            return 1;
        }
        if (rotation == 3) {
            return 2;
        }
        return rotation == 2 ? 3 : 0;
    }
}
