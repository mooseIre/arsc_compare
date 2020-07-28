package com.android.systemui;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import com.android.systemui.plugins.R;
import java.util.Arrays;

public class MCCUtils {
    public static boolean sIsIROperation;
    public static boolean sIsMXOperation;
    public static boolean sIsNPOperation;
    public static boolean sIsUSAOperation;

    public static void checkOperation(Context context, String str) {
        if (!TextUtils.isEmpty(str)) {
            String substring = str.substring(0, 3);
            sIsUSAOperation = Arrays.asList(context.getResources().getStringArray(R.array.usa_mcc)).contains(substring);
            sIsMXOperation = context.getResources().getString(R.string.mx_mcc).equals(substring);
            sIsIROperation = context.getResources().getString(R.string.ir_mcc).equals(substring);
            sIsNPOperation = context.getResources().getString(R.string.np_mcc).equals(substring);
        }
    }

    public static boolean isShowPlmnAndSpn(Context context, String str, boolean z) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        return getResourcesForOperation(context, str, z).getBoolean(R.bool.show_plmn_and_spn_in_carrier);
    }

    public static boolean isHideVolte(Context context, String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        return getResourcesForOperation(context, str, true).getBoolean(R.bool.status_bar_hide_volte);
    }

    public static boolean isShowSpnWhenAirplaneOn(Context context, String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        return getResourcesForOperation(context, str, true).getBoolean(R.bool.status_bar_show_spn_when_airplane);
    }

    public static boolean isMobileTypeShownWhenWifiOn(Context context, String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        return getResourcesForOperation(context, str, true).getBoolean(R.bool.status_bar_show_mobile_type_when_wifi_on);
    }

    public static boolean isShowMobileInMMS(Context context, String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        return getResourcesForOperation(context, str, true).getBoolean(R.bool.status_bar_show_mobile_type_in_mms);
    }

    public static Resources getResourcesForOperation(Context context, String str, boolean z) {
        if (TextUtils.isEmpty(str)) {
            return context.getResources();
        }
        Configuration configuration = context.getResources().getConfiguration();
        Configuration configuration2 = new Configuration();
        configuration2.setTo(configuration);
        int i = 0;
        int intValue = Integer.valueOf(str.substring(0, 3)).intValue();
        if (z) {
            i = Integer.valueOf(str.substring(3, str.length())).intValue();
        }
        configuration2.mcc = intValue;
        configuration2.mnc = i;
        if (configuration2.mnc == 0) {
            configuration2.mnc = 65535;
        }
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        DisplayMetrics displayMetrics2 = new DisplayMetrics();
        displayMetrics2.setTo(displayMetrics);
        return new Resources(context.getResources().getAssets(), displayMetrics2, configuration2);
    }
}
