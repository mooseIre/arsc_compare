package com.android.keyguard.wallpaper;

import android.content.Context;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.magazine.LockScreenMagazineUtils;
import com.android.keyguard.utils.ContentProviderUtils;
import com.android.systemui.plugins.R;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import miui.os.Build;

public class WallpaperAuthorityUtils {
    public static final String APPLY_MAGAZINE_DEFAULT_AUTHORITY = LockScreenMagazineUtils.PROVIDER_URI_LOCK_MAGAZINE_DEFAULT;
    private static List<String> sDeviceSupportVideo24WallpaperList = new ArrayList();

    public static String getWallpaperAuthority(Context context) {
        String stringForUser = Settings.System.getStringForUser(context.getContentResolver(), "lock_wallpaper_provider_authority", KeyguardUpdateMonitor.getCurrentUser());
        return TextUtils.isEmpty(stringForUser) ? "com.miui.home.none_provider" : stringForUser;
    }

    private static boolean supportVideo24Wallpaper(Context context) {
        if (sDeviceSupportVideo24WallpaperList.isEmpty()) {
            sDeviceSupportVideo24WallpaperList = Arrays.asList(context.getResources().getStringArray(R.array.device_support_video_24_wallpaper));
        }
        return sDeviceSupportVideo24WallpaperList.contains(Build.DEVICE);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x000c, code lost:
        r1 = com.android.keyguard.wallpaper.KeyguardWallpaperUtils.getWallpaperInfo();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isVideo24Wallpaper(android.content.Context r1) {
        /*
            boolean r0 = supportVideo24Wallpaper(r1)
            if (r0 == 0) goto L_0x0020
            boolean r1 = isHomeDefaultWallpaper(r1)
            if (r1 == 0) goto L_0x0020
            android.app.WallpaperInfo r1 = com.android.keyguard.wallpaper.KeyguardWallpaperUtils.getWallpaperInfo()
            if (r1 == 0) goto L_0x0020
            java.lang.String r1 = r1.getServiceName()
            java.lang.String r0 = "com.android.systemui.wallpaper.Video24WallpaperService"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0020
            r1 = 1
            return r1
        L_0x0020:
            r1 = 0
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.wallpaper.WallpaperAuthorityUtils.isVideo24Wallpaper(android.content.Context):boolean");
    }

    public static boolean setWallpaperAuthoritySystemSetting(Context context, String str) {
        return Settings.System.putStringForUser(context.getContentResolver(), "lock_wallpaper_provider_authority", str, KeyguardUpdateMonitor.getCurrentUser());
    }

    public static boolean isThemeLockLiveWallpaper(Context context) {
        if (isVideo24Wallpaper(context)) {
            return true;
        }
        return "com.android.thememanager.theme_lock_live_wallpaper".equals(getWallpaperAuthority(context));
    }

    public static boolean isThemeLockVideoWallpaper(Context context) {
        return "com.android.thememanager.theme_lock_video_wallpaper".equals(getWallpaperAuthority(context));
    }

    public static boolean isLiveWallpaper(Context context) {
        return isThemeLockLiveWallpaper(context) || isThemeLockVideoWallpaper(context);
    }

    public static boolean isHomeDefaultWallpaper(Context context) {
        return "com.miui.home.none_provider".equals(getWallpaperAuthority(context));
    }

    public static boolean isLockScreenMagazineWallpaper(Context context) {
        return isLockScreenMagazineOpenedWallpaper(context) || isLockScreenMagazineClosedWallpaper(context);
    }

    public static boolean isLockScreenMagazineOpenedWallpaper(Context context) {
        return APPLY_MAGAZINE_DEFAULT_AUTHORITY.equals(getWallpaperAuthority(context));
    }

    public static boolean isLockScreenMagazineClosedWallpaper(Context context) {
        return "com.xiaomi.tv.gallerylockscreen.set_lockwallpaper".equals(getWallpaperAuthority(context));
    }

    public static boolean isThemeLockWallpaper(Context context) {
        return "com.android.thememanager.theme_lockwallpaper".equals(getWallpaperAuthority(context));
    }

    public static boolean isCustomWallpaper(Context context) {
        String wallpaperAuthority = getWallpaperAuthority(context);
        return "com.android.thememanager.set_lockwallpaper".equals(wallpaperAuthority) || "com.android.thememanager.theme_lock_live_wallpaper".equals(wallpaperAuthority) || "com.android.thememanager.theme_lock_video_wallpaper".equals(wallpaperAuthority);
    }

    public static boolean isGalleryCloudBabyWallpaper(Context context) {
        return "com.miui.gallery.cloud.baby.wallpaper_provider".equals(getWallpaperAuthority(context));
    }

    public static boolean isValidAuthority(Context context) {
        String wallpaperAuthority = getWallpaperAuthority(context);
        if (TextUtils.isEmpty(wallpaperAuthority) || "com.miui.home.none_provider".equals(wallpaperAuthority)) {
            return false;
        }
        return ContentProviderUtils.isProviderExists(context, Uri.parse("content://" + wallpaperAuthority));
    }
}
