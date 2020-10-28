package com.android.keyguard.analytics;

import android.content.Context;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.MiuiKeyguardUtils;
import com.android.keyguard.wallpaper.WallpaperAuthorityUtils;
import com.xiaomi.stat.MiStat;
import java.util.HashMap;

public class LockScreenMagazineAnalytics {
    public static HashMap getLockScreenWallperProviderStatus(Context context) {
        HashMap hashMap = new HashMap();
        hashMap.put(MiStat.Param.STATUS, WallpaperAuthorityUtils.getWallpaperAuthority(context));
        boolean isDefaultLockScreenTheme = MiuiKeyguardUtils.isDefaultLockScreenTheme();
        AnalyticsHelper.booleanToInt(isDefaultLockScreenTheme);
        hashMap.put("isDefaultLockScreenTheme", Integer.valueOf(isDefaultLockScreenTheme ? 1 : 0));
        return hashMap;
    }

    public static HashMap getLockScreenMagazinePreviewActionParams(Context context, String str) {
        HashMap hashMap = new HashMap();
        hashMap.put("action", str);
        return hashMap;
    }

    public static HashMap getNegativeStatusParams(Context context) {
        HashMap hashMap = new HashMap();
        hashMap.put(MiStat.Param.STATUS, KeyguardUpdateMonitor.getInstance(context).isSupportLockScreenMagazineLeft() ? "lockScreenMagazine" : "controlCenter");
        return hashMap;
    }
}
