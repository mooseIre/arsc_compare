package com.android.keyguard.analytics;

import com.android.keyguard.magazine.LockScreenMagazineController;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.keyguard.wallpaper.WallpaperAuthorityUtils;
import com.android.systemui.Dependency;
import java.util.HashMap;

public class LockScreenMagazineAnalytics {
    public static HashMap getLockScreenWallperProviderStatus() {
        HashMap hashMap = new HashMap();
        hashMap.put("status", WallpaperAuthorityUtils.getWallpaperAuthority());
        boolean isDefaultLockScreenTheme = MiuiKeyguardUtils.isDefaultLockScreenTheme();
        AnalyticsHelper.booleanToInt(isDefaultLockScreenTheme);
        hashMap.put("isDefaultLockScreenTheme", Integer.valueOf(isDefaultLockScreenTheme ? 1 : 0));
        return hashMap;
    }

    public static HashMap getLockScreenMagazinePreviewActionParams(String str) {
        HashMap hashMap = new HashMap();
        hashMap.put("action", str);
        return hashMap;
    }

    public static HashMap getNegativeStatusParams() {
        HashMap hashMap = new HashMap();
        hashMap.put("status", ((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).isSupportLockScreenMagazineLeft() ? "lockScreenMagazine" : "controlCenter");
        return hashMap;
    }
}
