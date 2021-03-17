package com.android.keyguard.magazine.utils;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.analytics.AnalyticsHelper;
import com.android.keyguard.faceunlock.MiuiFaceUnlockManager;
import com.android.keyguard.magazine.LockScreenMagazineController;
import com.android.keyguard.utils.ContentProviderUtils;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.keyguard.wallpaper.MiuiKeyguardWallpaperControllerImpl;
import com.android.keyguard.wallpaper.WallpaperAuthorityUtils;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.phone.StatusBar;
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
            sendLockScreenMagazineEventBroadcast(context, str, null);
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

    public static void sendLockScreenMagazineScreenOnBroadcast(Context context) {
        StatusBar statusBar = (StatusBar) Dependency.get(StatusBar.class);
        if (statusBar != null) {
            Bundle bundle = new Bundle();
            bundle.putInt("notification_count", statusBar.getKeyguardNotifications());
            sendLockScreenMagazineEventBroadcast(context, "Screen_ON", bundle);
        }
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
                    str = ((MiuiFaceUnlockManager) Dependency.get(MiuiFaceUnlockManager.class)).isStayScreenWhenFaceUnlockSuccess() ? "FaceSwipe" : "FaceDirect";
                } else if (c == 3) {
                    str = "Band";
                } else if (c == 4) {
                    str = "SmartLock";
                }
                Bundle bundle = new Bundle();
                bundle.putString("unlock_type", str);
                sendLockScreenMagazineEventBroadcast(context, "Device_Unlock", bundle);
            }
            int i = AnonymousClass2.$SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode[((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).getSecurityMode().ordinal()];
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

    /* renamed from: com.android.keyguard.magazine.utils.LockScreenMagazineUtils$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
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
                com.android.keyguard.magazine.utils.LockScreenMagazineUtils.AnonymousClass2.$SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode = r0
                com.android.keyguard.KeyguardSecurityModel$SecurityMode r1 = com.android.keyguard.KeyguardSecurityModel.SecurityMode.PIN     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = com.android.keyguard.magazine.utils.LockScreenMagazineUtils.AnonymousClass2.$SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode     // Catch:{ NoSuchFieldError -> 0x001d }
                com.android.keyguard.KeyguardSecurityModel$SecurityMode r1 = com.android.keyguard.KeyguardSecurityModel.SecurityMode.Password     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = com.android.keyguard.magazine.utils.LockScreenMagazineUtils.AnonymousClass2.$SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.android.keyguard.KeyguardSecurityModel$SecurityMode r1 = com.android.keyguard.KeyguardSecurityModel.SecurityMode.Pattern     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.magazine.utils.LockScreenMagazineUtils.AnonymousClass2.<clinit>():void");
        }
    }

    private static void notifyRecordEvent(final Context context, final String str, final Bundle bundle) {
        new AsyncTask<Void, Void, Void>() {
            /* class com.android.keyguard.magazine.utils.LockScreenMagazineUtils.AnonymousClass1 */

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
