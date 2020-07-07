package com.android.systemui;

import android.content.Context;
import android.content.res.Resources;
import android.os.SystemProperties;
import com.android.systemui.plugins.R;
import java.util.ArrayList;
import java.util.List;
import miui.os.Build;

public class CustomizedUtils {
    private static boolean isCustomized = (Build.IS_CT_CUSTOMIZATION_TEST || Build.IS_CU_CUSTOMIZATION_TEST || Build.IS_CM_CUSTOMIZATION_TEST);
    private static String sLastRegion;
    private static boolean sShowCarrierInHeader;
    private static final List<String> sShowCarrierRegions;

    static {
        ArrayList arrayList = new ArrayList();
        sShowCarrierRegions = arrayList;
        arrayList.add("TW");
    }

    public static boolean isCarrierInHeaderViewShown() {
        return sShowCarrierInHeader;
    }

    public static void setCustomized(Context context) {
        isCustomized = isCustomized || context.getResources().getBoolean(R.bool.show_carrier_in_status_bar_header);
    }

    public static int getNotchExpandedHeaderViewHeight(Context context, int i) {
        if (!sShowCarrierInHeader) {
            return i;
        }
        if (Resources.getSystem().getConfiguration().orientation == 2) {
            return context.getResources().getDimensionPixelSize(R.dimen.status_bar_height);
        }
        return context.getResources().getDimensionPixelSize(R.dimen.expanded_header_with_carrier_height);
    }

    public static void checkRegion() {
        sLastRegion = SystemProperties.get("ro.miui.region", "");
        updateShowCarrierInHeader();
    }

    private static void updateShowCarrierInHeader() {
        sShowCarrierInHeader = isCustomized || sShowCarrierRegions.contains(sLastRegion);
    }
}
