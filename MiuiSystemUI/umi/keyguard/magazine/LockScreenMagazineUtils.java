package com.android.keyguard.magazine;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MiuiSettings;
import android.util.Log;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.MiuiKeyguardUtils;
import com.android.keyguard.analytics.AnalyticsHelper;
import com.android.keyguard.faceunlock.FaceUnlockManager;
import com.android.keyguard.magazine.mode.LockScreenMagazineWallpaperInfo;
import com.android.keyguard.utils.ContentProviderUtils;
import com.android.keyguard.utils.PreferenceUtils;
import com.android.keyguard.wallpaper.KeyguardWallpaperUtils;
import com.android.keyguard.wallpaper.WallpaperAuthorityUtils;
import com.android.systemui.Constants;
import com.android.systemui.SystemUI;
import com.android.systemui.statusbar.phone.StatusBar;
import com.google.gson.Gson;
import miui.os.Build;

public class LockScreenMagazineUtils {
    public static final String CONTENT_URI_LOCK_MAGAZINE_DEFAULT = ("content://" + PROVIDER_URI_LOCK_MAGAZINE_DEFAULT);
    public static final String LOCK_SCREEN_MAGAZINE_PACKAGE_NAME = (Constants.IS_INTERNATIONAL ? "com.miui.android.fashiongallery" : "com.mfashiongallery.emag");
    public static final String PROVIDER_URI_LOCK_MAGAZINE_DEFAULT = (Build.IS_INTERNATIONAL_BUILD ? "com.miui.android.fashiongallery.lockscreen_magazine_provider" : "com.xiaomi.tv.gallerylockscreen.lockscreen_magazine_provider");
    public static String SYSTEM_SETTINGS_KEY_LOCKSCREEN_MAGAZINE_STATUS = "lock_screen_magazine_status";

    public static void gotoLockScreenMagazine(Context context, String str) {
        try {
            Intent intent = new Intent("android.miui.UPDATE_LOCKSCREEN_WALLPAPER");
            intent.putExtra("showtime", System.currentTimeMillis() + 600);
            intent.putExtra("startTime", System.currentTimeMillis());
            intent.putExtra("from", str);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        } catch (Exception e) {
            AnalyticsHelper.getInstance(context).record("keyguard_goto_lockscreen_magazine_fail");
            Log.e("LockScreenMagazineUtils", e.toString());
        }
    }

    public static boolean getLockScreenMagazineStatus(Context context) {
        return MiuiSettings.Secure.getBoolean(context.getContentResolver(), SYSTEM_SETTINGS_KEY_LOCKSCREEN_MAGAZINE_STATUS, false);
    }

    public static void setLockScreenMagazineStatus(Context context, boolean z) {
        MiuiSettings.Secure.putBoolean(context.getContentResolver(), SYSTEM_SETTINGS_KEY_LOCKSCREEN_MAGAZINE_STATUS, z);
    }

    public static LockScreenMagazineWallpaperInfo getLockScreenMagazineWallpaperInfo(Context context) {
        LockScreenMagazineWallpaperInfo lockScreenMagazineWallpaperInfo;
        try {
            lockScreenMagazineWallpaperInfo = (LockScreenMagazineWallpaperInfo) new Gson().fromJson(KeyguardWallpaperUtils.getCurrentWallpaperInfo(context), LockScreenMagazineWallpaperInfo.class);
        } catch (Exception e) {
            Log.e("LockScreenMagazineUtils", "getLockScreenMagazineWallpaperInfo" + e.getMessage());
            lockScreenMagazineWallpaperInfo = null;
        }
        if (lockScreenMagazineWallpaperInfo == null) {
            lockScreenMagazineWallpaperInfo = new LockScreenMagazineWallpaperInfo();
        }
        lockScreenMagazineWallpaperInfo.initExtra();
        return lockScreenMagazineWallpaperInfo;
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

    public static void notifySubscriptionChange(final Context context) {
        new AsyncTask<Void, Void, Void>() {
            /* access modifiers changed from: protected */
            public Void doInBackground(Void... voidArr) {
                ContentProviderUtils.getResultFromProvider(context, LockScreenMagazineUtils.CONTENT_URI_LOCK_MAGAZINE_DEFAULT, "subscriptionChange", (String) null, (Bundle) null);
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    public static boolean isLockScreenMagazineAvailable(Context context) {
        return !Constants.IS_TABLET && MiuiKeyguardUtils.isUserUnlocked(context) && MiuiKeyguardUtils.isDefaultLockScreenTheme();
    }

    public static void sendLockScreenMagazineEventBroadcast(Context context, String str) {
        if (isGlobalNeedFeature(context)) {
            sendLockScreenMagazineEventBroadcast(context, str, (Bundle) null);
        }
    }

    private static void sendLockScreenMagazineEventBroadcast(Context context, String str, Bundle bundle) {
        String str2 = KeyguardUpdateMonitor.getInstance(context).getLockScreenMagazineWallpaperInfo().wallpaperUri;
        Intent intent = new Intent("com.miui.systemui.LOCKSCREEN_WALLPAPER_EVENTS");
        intent.putExtra("wallpaper_uri", str2);
        intent.putExtra("wallpaper_view_event", str);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        intent.setPackage(LOCK_SCREEN_MAGAZINE_PACKAGE_NAME);
        context.sendBroadcast(intent);
    }

    public static void sendLockScreenMagazineScreenOnBroadcast(Context context) {
        Bundle bundle = new Bundle();
        bundle.putInt("notification_count", ((StatusBar) SystemUI.getComponent(context, StatusBar.class)).getKeyguardNotifications());
        sendLockScreenMagazineEventBroadcast(context, "Screen_ON", bundle);
    }

    public static void sendLockScreenMagazineUnlockBroadcast(Context context) {
        String str;
        if (isGlobalNeedFeature(context)) {
            String unlockWay = AnalyticsHelper.getInstance(context).getUnlockWay();
            char c = 65535;
            switch (unlockWay.hashCode()) {
                case -1806098975:
                    if (unlockWay.equals("smart_lock")) {
                        c = 4;
                        break;
                    }
                    break;
                case 3274:
                    if (unlockWay.equals("fp")) {
                        c = 1;
                        break;
                    }
                    break;
                case 3591:
                    if (unlockWay.equals("pw")) {
                        c = 0;
                        break;
                    }
                    break;
                case 3016245:
                    if (unlockWay.equals("band")) {
                        c = 3;
                        break;
                    }
                    break;
                case 3135069:
                    if (unlockWay.equals("face")) {
                        c = 2;
                        break;
                    }
                    break;
                case 3387192:
                    if (unlockWay.equals("none")) {
                        c = 5;
                        break;
                    }
                    break;
            }
            if (c != 0) {
                if (c == 1) {
                    str = "Fingerprint";
                } else if (c == 2) {
                    str = FaceUnlockManager.getInstance().isStayScreenWhenFaceUnlockSuccess() ? "FaceSwipe" : "FaceDirect";
                } else if (c == 3) {
                    str = "Band";
                } else if (c == 4) {
                    str = "SmartLock";
                }
                Bundle bundle = new Bundle();
                bundle.putString("unlock_type", str);
                sendLockScreenMagazineEventBroadcast(context, "Device_Unlock", bundle);
            }
            int i = AnonymousClass3.$SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode[LockScreenMagazineController.getInstance(context).getSecurityMode().ordinal()];
            if (i == 1) {
                str = "Pin";
            } else if (i == 2) {
                str = "Password";
            } else if (i == 3) {
                str = "Pattern";
            }
            Bundle bundle2 = new Bundle();
            bundle2.putString("unlock_type", str);
            sendLockScreenMagazineEventBroadcast(context, "Device_Unlock", bundle2);
            str = "NoScreenLock";
            Bundle bundle22 = new Bundle();
            bundle22.putString("unlock_type", str);
            sendLockScreenMagazineEventBroadcast(context, "Device_Unlock", bundle22);
        }
    }

    /* renamed from: com.android.keyguard.magazine.LockScreenMagazineUtils$3  reason: invalid class name */
    static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode;

        /* JADX WARNING: Can't wrap try/catch for region: R(6:0|1|2|3|4|(3:5|6|8)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        static {
            /*
                com.android.keyguard.KeyguardSecurityModel$SecurityMode[] r0 = com.android.keyguard.KeyguardSecurityModel.SecurityMode.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode = r0
                com.android.keyguard.KeyguardSecurityModel$SecurityMode r1 = com.android.keyguard.KeyguardSecurityModel.SecurityMode.PIN     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode     // Catch:{ NoSuchFieldError -> 0x001d }
                com.android.keyguard.KeyguardSecurityModel$SecurityMode r1 = com.android.keyguard.KeyguardSecurityModel.SecurityMode.Password     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.android.keyguard.KeyguardSecurityModel$SecurityMode r1 = com.android.keyguard.KeyguardSecurityModel.SecurityMode.Pattern     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.magazine.LockScreenMagazineUtils.AnonymousClass3.<clinit>():void");
        }
    }

    public static long getRequestLockScreenMagazineWallpaperTime(Context context) {
        return PreferenceUtils.getLong(context, "pref_key_request_lock_screen_magazine_wallpaper_time", 0);
    }

    public static boolean setRequestLockScreenMagazineWallpaperTime(Context context, long j) {
        PreferenceUtils.putLong(context, "pref_key_request_lock_screen_magazine_wallpaper_time", j);
        return true;
    }

    public static int getLockScreenMagazineWallpaperAutoUpdateMinute(Context context) {
        return PreferenceUtils.getInt(context, "pref_key_lock_screen_magazine_wallpaper_auto_update_minute", 180);
    }

    public static boolean setLockScreenMagazineWallpaperAutoUpdateMinute(Context context, int i) {
        PreferenceUtils.putInt(context, "pref_key_lock_screen_magazine_wallpaper_auto_update_minute", i);
        return true;
    }

    public static boolean checkLockScreenMagazineDecoupleHome(Context context) {
        Bundle resultFromProvider = ContentProviderUtils.getResultFromProvider(context, CONTENT_URI_LOCK_MAGAZINE_DEFAULT, "isDecoupleHome", (String) null, (Bundle) null);
        if (resultFromProvider != null) {
            return resultFromProvider.getBoolean("result_boolean");
        }
        return false;
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
            String str = KeyguardUpdateMonitor.getInstance(context).getLockScreenMagazineWallpaperInfo().wallpaperUri;
            Bundle bundle = new Bundle();
            bundle.putString("wallpaper_uri", str);
            notifyRecordEvent(context, "recordPreviewMode", bundle);
        }
    }

    private static boolean isGlobalNeedFeature(Context context) {
        return Build.IS_INTERNATIONAL_BUILD && KeyguardUpdateMonitor.getInstance(context).isSupportLockScreenMagazineLeft() && WallpaperAuthorityUtils.isLockScreenMagazineOpenedWallpaper(context);
    }
}
