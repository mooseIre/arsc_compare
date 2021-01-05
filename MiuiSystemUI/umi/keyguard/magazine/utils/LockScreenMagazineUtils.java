package com.android.keyguard.magazine.utils;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.analytics.AnalyticsHelper;
import com.android.keyguard.magazine.LockScreenMagazineController;
import com.android.keyguard.utils.ContentProviderUtils;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.keyguard.wallpaper.MiuiKeyguardWallpaperControllerImpl;
import com.android.keyguard.wallpaper.WallpaperAuthorityUtils;
import com.android.systemui.Dependency;
import com.miui.systemui.BuildConfig;
import com.miui.systemui.util.MiuiTextUtils;
import miui.os.Build;

public class LockScreenMagazineUtils {
    public static final String CONTENT_URI_LOCK_MAGAZINE_DEFAULT = ("content://" + PROVIDER_URI_LOCK_MAGAZINE_DEFAULT);
    public static final String LOCK_SCREEN_MAGAZINE_PACKAGE_NAME = (BuildConfig.IS_INTERNATIONAL ? "com.miui.android.fashiongallery" : "com.mfashiongallery.emag");
    public static final String PROVIDER_URI_LOCK_MAGAZINE_DEFAULT = (Build.IS_INTERNATIONAL_BUILD ? "com.miui.android.fashiongallery.lockscreen_magazine_provider" : "com.xiaomi.tv.gallerylockscreen.lockscreen_magazine_provider");

    public static void gotoMagazine(Context context, String str) {
        if (MiuiTextUtils.isNotEmpty(((MiuiKeyguardWallpaperControllerImpl) Dependency.get(MiuiKeyguardWallpaperControllerImpl.class)).getCurrentWallpaperString())) {
            ((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).startMagazineActivity(System.currentTimeMillis() + 600);
            return;
        }
        Log.e("LockScreenMagazineUtils", "gotoMagazine fail, WallpaperString == null ");
        AnalyticsHelper.getInstance(context).record("keyguard_goto_lockscreen_magazine_fail");
    }

    public static Bundle getLockScreenMagazinePreContent(Context context) {
        return ContentProviderUtils.getResultFromProvider(context, CONTENT_URI_LOCK_MAGAZINE_DEFAULT, "getTransitionInfo", (String) null, (Bundle) null);
    }

    public static String getLockScreenMagazineSettingsDeepLink(Context context) {
        Bundle resultFromProvider = ContentProviderUtils.getResultFromProvider(context, CONTENT_URI_LOCK_MAGAZINE_DEFAULT, "getAppSettingsDeeplink", (String) null, (Bundle) null);
        if (resultFromProvider != null) {
            return resultFromProvider.getString("result_string");
        }
        return null;
    }

    public static boolean isLockScreenMagazineAvailable() {
        return ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isUserUnlocked(KeyguardUpdateMonitor.getCurrentUser()) && MiuiKeyguardUtils.isDefaultLockScreenTheme();
    }

    public static void sendLockScreenMagazineEventBroadcast(Context context, String str) {
        if (isGlobalNeedFeature(context)) {
            sendLockScreenMagazineEventBroadcast(context, str, (Bundle) null);
        }
    }

    private static void sendLockScreenMagazineEventBroadcast(Context context, String str, Bundle bundle) {
        String str2 = ((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).getLockScreenMagazineWallpaperInfo().wallpaperUri;
        Intent intent = new Intent("com.miui.systemui.LOCKSCREEN_WALLPAPER_EVENTS");
        intent.putExtra("wallpaper_uri", str2);
        intent.putExtra("wallpaper_view_event", str);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        intent.setPackage(LOCK_SCREEN_MAGAZINE_PACKAGE_NAME);
        context.sendBroadcast(intent);
    }

    private static void notifyRecordEvent(final Context context, final String str, final Bundle bundle) {
        new AsyncTask<Void, Void, Void>() {
            /* access modifiers changed from: protected */
            public Void doInBackground(Void... voidArr) {
                ContentProviderUtils.getResultFromProvider(context, LockScreenMagazineUtils.CONTENT_URI_LOCK_MAGAZINE_DEFAULT, str, (String) null, bundle);
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    public static void notifyFullScreenClickRecordEvent(Context context) {
        if (isGlobalNeedFeature(context)) {
            String str = ((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).getLockScreenMagazineWallpaperInfo().wallpaperUri;
            Bundle bundle = new Bundle();
            bundle.putString("wallpaper_uri", str);
            notifyRecordEvent(context, "recordPreviewMode", bundle);
        }
    }

    private static boolean isGlobalNeedFeature(Context context) {
        return Build.IS_INTERNATIONAL_BUILD && ((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).isSupportLockScreenMagazineLeft() && WallpaperAuthorityUtils.isLockScreenMagazineOpenedWallpaper();
    }
}
