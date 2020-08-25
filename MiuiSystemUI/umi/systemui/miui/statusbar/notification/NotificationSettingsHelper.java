package com.android.systemui.miui.statusbar.notification;

import android.app.INotificationManager;
import android.app.NotificationChannelCompat;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.miui.AppOpsUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Log;
import android.util.Slog;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import miui.os.Build;
import miui.securityspace.XSpaceUserHandle;
import miui.util.NotificationFilterHelper;

public class NotificationSettingsHelper {
    private static final boolean DEBUG = Constants.DEBUG;
    static INotificationManager sINM = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));

    public static boolean isNotificationsBanned(Context context, String str) {
        return isNotificationsBanned(str, getPackageUid(context, str));
    }

    public static boolean isNotificationsBanned(String str, int i) {
        try {
            return !sINM.areNotificationsEnabledForPackage(str, i);
        } catch (Exception e) {
            Slog.e("NotifiSettingsHelper", "Error areNotificationsEnabledForPackage " + e);
            return false;
        }
    }

    public static void setNotificationsEnabledForPackage(Context context, String str, boolean z) {
        setNotificationsEnabledForPackage(context, str, getPackageUid(context, str), z);
    }

    public static void setNotificationsEnabledForPackage(Context context, String str, int i, boolean z) {
        try {
            if (XSpaceUserHandle.isUidBelongtoXSpace(i)) {
                setNotificationsEnabledForPackage(str, getPackageUid(context.createPackageContextAsUser(str, 2, UserHandle.OWNER), str), z);
            } else if (context.getUserId() == 0 && XSpaceUserHandle.isAppInXSpace(context, str)) {
                setNotificationsEnabledForPackage(str, getPackageUid(context.createPackageContextAsUser(str, 2, new UserHandle(999)), str), z);
            }
        } catch (Exception unused) {
        }
        setNotificationsEnabledForPackage(str, i, z);
        try {
            if (!Build.IS_TABLET || Build.VERSION.SDK_INT >= 26) {
                int i2 = 0;
                ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(str, 0);
                if (applicationInfo != null && (applicationInfo.flags & 1) == 0) {
                    if (!z) {
                        i2 = 1;
                    }
                    AppOpsUtils.setMode(context, 11, str, i2);
                }
            }
        } catch (Exception unused2) {
        }
    }

    private static void setNotificationsEnabledForPackage(String str, int i, boolean z) {
        try {
            sINM.setNotificationsEnabledForPackage(str, i, z);
        } catch (Exception unused) {
        }
    }

    public static boolean isFoldable(Context context, String str) {
        String[] stringArray = context.getResources().getStringArray(17236056);
        if (stringArray == null || stringArray.length <= 0) {
            return true;
        }
        for (String equals : stringArray) {
            if (str.equals(equals)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNonBlockable(Context context, String str, String str2) {
        String[] stringArray = context.getResources().getStringArray(17236056);
        if (stringArray != null) {
            int length = stringArray.length;
            for (int i = 0; i < length; i++) {
                String str3 = stringArray[i];
                if (str3 != null) {
                    if (str3.contains(":")) {
                        String[] split = str3.split(":", 2);
                        if (str.equals(split[0]) && str2.equals(split[1])) {
                            return true;
                        }
                    } else if (str.equals(stringArray[i])) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void setFoldImportance(Context context, String str, int i) {
        NotificationFilterHelper.setImportance(context, str, i);
        if (isUserOwner(context)) {
            ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).setFoldImportance(context, str, i);
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString("package", str);
        bundle.putInt("foldImportance", i);
        try {
            context.getContentResolver().call(Uri.parse("content://statusbar.notification"), "setFoldImportance", (String) null, bundle);
        } catch (Exception e) {
            Log.d("NotifiSettingsHelper", "Error setFoldImportance " + e);
        }
    }

    public static boolean checkFloat(Context context, String str, String str2) {
        if (isUserOwner(context)) {
            return ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).canFloat(context, str, str2);
        }
        Bundle bundle = new Bundle();
        bundle.putString("package", str);
        bundle.putString("channel_id", str2);
        try {
            Bundle call = context.getContentResolver().call(Uri.parse("content://statusbar.notification"), "canFloat", (String) null, bundle);
            if (call != null) {
                return call.getBoolean("canShowFloat");
            }
            return false;
        } catch (Exception e) {
            Log.d("NotifiSettingsHelper", "Error canFloat " + e);
            return false;
        }
    }

    public static boolean checkKeyguard(Context context, String str, String str2) {
        if (isUserOwner(context)) {
            return ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).canShowOnKeyguard(context, str, str2);
        }
        Bundle bundle = new Bundle();
        bundle.putString("package", str);
        bundle.putString("channel_id", (String) null);
        try {
            Bundle call = context.getContentResolver().call(Uri.parse("content://statusbar.notification"), "canShowOnKeyguard", str2, bundle);
            if (call != null) {
                return call.getBoolean("canShowOnKeyguard");
            }
            return false;
        } catch (Exception e) {
            Log.d("NotifiSettingsHelper", "Error canShowKeyguard " + e);
            return false;
        }
    }

    public static boolean canShowBadge(Context context, String str) {
        return canShowBadge(context, str, (NotificationChannelCompat) null);
    }

    public static boolean canShowBadge(Context context, String str, NotificationChannelCompat notificationChannelCompat) {
        boolean z;
        if (isUserOwner(context)) {
            z = ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).canShowBadge(context, str);
        } else {
            Bundle bundle = new Bundle();
            bundle.putString("package", str);
            try {
                Bundle call = context.getContentResolver().call(Uri.parse("content://statusbar.notification"), "canShowBadge", (String) null, bundle);
                if (call != null) {
                    z = call.getBoolean("canShowBadge");
                }
            } catch (Exception e) {
                Log.d("NotifiSettingsHelper", "Error canShowBadge " + e);
            }
            z = false;
        }
        if (!z) {
            return false;
        }
        if (notificationChannelCompat == null || notificationChannelCompat.canShowBadge()) {
            return true;
        }
        return false;
    }

    private static boolean isUserOwner(Context context) {
        return context.getUserId() == 0;
    }

    public static boolean isUidSystem(int i) {
        int appId = UserHandle.getAppId(i);
        return appId == 1000 || appId == 1001 || i == 0;
    }

    public static int getPackageUid(Context context, String str) {
        try {
            return context.getPackageManager().getPackageUid(str, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Slog.e("NotifiSettingsHelper", "Error getPackageUid " + e);
            return 0;
        }
    }

    public static void startAppNotificationSettings(Context context, String str, String str2, int i, String str3) {
        if (DEBUG) {
            Log.d("NotifiSettingsHelper", String.format("startAppNotificationSettings pkg=%s label=%s uid=%s", new Object[]{str, str2, Integer.valueOf(i)}));
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addFlags(32768);
        intent.addFlags(268435456);
        intent.setClassName("com.android.settings", "com.android.settings.Settings$AppNotificationSettingsActivity");
        intent.putExtra("package", str);
        intent.putExtra("uid", i);
        try {
            context.startActivityAsUser(intent, UserHandle.CURRENT);
        } catch (ActivityNotFoundException unused) {
        }
    }
}
