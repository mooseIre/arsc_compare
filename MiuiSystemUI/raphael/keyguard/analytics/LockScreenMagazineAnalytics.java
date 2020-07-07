package com.android.keyguard.analytics;

import android.content.Context;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.MiuiKeyguardUtils;
import com.android.keyguard.wallpaper.WallpaperAuthorityUtils;
import com.xiaomi.stat.MiStat;
import com.xiaomi.stat.MiStatParams;
import java.util.Locale;

public class LockScreenMagazineAnalytics {
    private static MiStatParams getBaseParams(Context context) {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putString("lockScreenMagazineStatus", WallpaperAuthorityUtils.isLockScreenMagazineOpenedWallpaper(context) ? "open" : "close");
        miStatParams.putString("currentLanguage", Locale.getDefault().getLanguage());
        return miStatParams;
    }

    public static MiStatParams getLockScreenWallperProviderStatus(Context context) {
        MiStatParams baseParams = getBaseParams(context);
        baseParams.putString(MiStat.Param.STATUS, WallpaperAuthorityUtils.getWallpaperAuthority(context));
        boolean isDefaultLockScreenTheme = MiuiKeyguardUtils.isDefaultLockScreenTheme();
        AnalyticsHelper.booleanToInt(isDefaultLockScreenTheme);
        baseParams.putInt("isDefaultLockScreenTheme", isDefaultLockScreenTheme ? 1 : 0);
        return baseParams;
    }

    public static MiStatParams getLockScreenMagazinePreviewActionParams(Context context, String str) {
        MiStatParams baseParams = getBaseParams(context);
        baseParams.putString("action", str);
        return baseParams;
    }

    public static MiStatParams getNegativeStatusParams(Context context) {
        MiStatParams baseParams = getBaseParams(context);
        baseParams.putString(MiStat.Param.STATUS, KeyguardUpdateMonitor.getInstance(context).isSupportLockScreenMagazineLeft() ? "lockScreenMagazine" : "controlCenter");
        return baseParams;
    }
}
