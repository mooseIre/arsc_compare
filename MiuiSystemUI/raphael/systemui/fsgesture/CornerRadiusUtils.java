package com.android.systemui.fsgesture;

import android.content.Context;
import android.content.res.Resources;

public class CornerRadiusUtils {
    public static int getPhoneRadius(Context context) {
        return getPhoneRadius(context, "rounded_corner_radius_top");
    }

    public static int getPhoneRadius(Context context, String str) {
        int i;
        Resources resources = context.getResources();
        if ("rounded_corner_radius_top".equals(str)) {
            i = resources.getIdentifier("rounded_corner_radius_top", "dimen", "android");
        } else {
            i = resources.getIdentifier("rounded_corner_radius_bottom", "dimen", "android");
        }
        if (i > 0) {
            return resources.getDimensionPixelSize(i);
        }
        return 0;
    }
}
