package com.android.systemui;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

public class MCCUtils {
    public static boolean isShowSpnWhenAirplaneOn(Context context, String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        return getResourcesForOperation(context, str, true).getBoolean(C0010R$bool.status_bar_show_spn_when_airplane);
    }

    public static boolean isShowSpnByGidWhenAirplaneOn(Context context, String str, String str2) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        String[] stringArray = getResourcesForOperation(context, str, true).getStringArray(C0008R$array.status_bar_show_spn_by_gid_when_airplane);
        Log.d("MCCUtils", "isShowSpnByGidWhenAirplaneOn: operation = " + str + "; gid = " + str2);
        for (int i = 0; i < stringArray.length; i++) {
            Log.d("MCCUtils", "isShowSpnByGidWhenAirplaneOn: cus_gid_values = " + stringArray[i]);
            if (stringArray[i].equals(str2)) {
                return true;
            }
        }
        return false;
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
        if (i == 0) {
            configuration2.mnc = 65535;
        }
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        DisplayMetrics displayMetrics2 = new DisplayMetrics();
        displayMetrics2.setTo(displayMetrics);
        return new Resources(context.getResources().getAssets(), displayMetrics2, configuration2);
    }
}
