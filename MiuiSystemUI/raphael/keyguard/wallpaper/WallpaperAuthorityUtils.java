package com.android.keyguard.wallpaper;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import com.android.keyguard.magazine.utils.LockScreenMagazineUtils;
import com.android.keyguard.utils.ContentProviderUtils;
import com.android.systemui.Dependency;
import com.miui.systemui.SettingsObserver;

public class WallpaperAuthorityUtils {
    public static final String APPLY_MAGAZINE_DEFAULT_AUTHORITY = LockScreenMagazineUtils.PROVIDER_URI_LOCK_MAGAZINE_DEFAULT;

    public static String getWallpaperAuthority() {
        String value = ((SettingsObserver) Dependency.get(SettingsObserver.class)).getValue("lock_wallpaper_provider_authority");
        return TextUtils.isEmpty(value) ? "com.miui.home.none_provider" : value;
    }

    public static boolean isThemeLockLiveWallpaper() {
        return "com.android.thememanager.theme_lock_live_wallpaper".equals(getWallpaperAuthority());
    }

    public static boolean isThemeLockVideoWallpaper() {
        return "com.android.thememanager.theme_lock_video_wallpaper".equals(getWallpaperAuthority());
    }

    public static boolean isHomeDefaultWallpaper() {
        return "com.miui.home.none_provider".equals(getWallpaperAuthority());
    }

    public static boolean isLockScreenMagazineWallpaper() {
        return isLockScreenMagazineOpenedWallpaper() || isLockScreenMagazineClosedWallpaper();
    }

    public static boolean isLockScreenMagazineOpenedWallpaper() {
        return APPLY_MAGAZINE_DEFAULT_AUTHORITY.equals(getWallpaperAuthority());
    }

    public static boolean isLockScreenMagazineClosedWallpaper() {
        return "com.xiaomi.tv.gallerylockscreen.set_lockwallpaper".equals(getWallpaperAuthority());
    }

    public static boolean isValidWallpaperAuthority(Context context) {
        String wallpaperAuthority = getWallpaperAuthority();
        if (TextUtils.isEmpty(wallpaperAuthority) || "com.miui.home.none_provider".equals(wallpaperAuthority)) {
            return false;
        }
        return ContentProviderUtils.isProviderExists(context, Uri.parse("content://" + wallpaperAuthority));
    }
}
