package com.android.systemui.qs.tiles;

import android.content.Context;
import android.location.LocationManager;
import android.os.UserHandle;
import com.android.settingslib.Utils;
import com.xiaomi.stat.MiStat;

public class TilesHelper {
    public static boolean setLocationProviderEnabledForUser(Context context, String str, boolean z, int i) {
        return ((LocationManager) context.getSystemService(MiStat.Param.LOCATION)).setProviderEnabledForUser(str, z, UserHandle.of(i));
    }

    public static boolean isLocationProviderEnabledForUser(Context context, String str, int i) {
        return ((LocationManager) context.getSystemService(MiStat.Param.LOCATION)).isProviderEnabledForUser(str, UserHandle.of(i));
    }

    public static boolean updateLocationEnabled(Context context, boolean z, int i) {
        Utils.updateLocationEnabled(context, z, i, 2);
        return true;
    }

    public static boolean isLocationEnabled(Context context, int i) {
        return ((LocationManager) context.getSystemService(MiStat.Param.LOCATION)).isLocationEnabledForUser(UserHandle.of(i));
    }
}
