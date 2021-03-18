package com.android.systemui.statusbar.notification;

import android.app.INotificationManager;
import android.app.NotificationChannel;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.miui.AppOpsUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Log;
import android.util.Slog;
import com.android.systemui.C0010R$bool;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.miui.systemui.DebugConfig;
import com.miui.systemui.SettingsManager;
import miui.os.Build;
import miui.securityspace.XSpaceUserHandle;
import miui.util.NotificationFilterHelper;

public class NotificationSettingsHelper {
    private static final boolean DEBUG = DebugConfig.DEBUG_NOTIFICATION;
    static INotificationManager sINM = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));

    public static int getNotificationStyle() {
        return ((SettingsManager) Dependency.get(SettingsManager.class)).getNotifStyle();
    }

    public static boolean showMiuiStyle() {
        return getNotificationStyle() == 0;
    }

    public static boolean showGoogleStyle() {
        return getNotificationStyle() == 1;
    }

    public static boolean alwaysShowKeyguardNotifications() {
        return ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).getBoolean(C0010R$bool.kept_notifications_on_keyguard);
    }

    public static boolean shouldRunPeekAnimation() {
        return ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).getBoolean(C0010R$bool.config_runPeekAnimation);
    }

    public static boolean isNotificationsBanned(Context context, String str) {
        return isNotificationsBanned(str, NotificationUtil.getPackageUid(context, str));
    }

    public static boolean isNotificationsBanned(String str, int i) {
        try {
            return !sINM.areNotificationsEnabledForPackage(str, i);
        } catch (Exception e) {
            Slog.e("NotifiSettingsHelper", "areNotificationsEnabledForPackage " + str, e);
            return false;
        }
    }

    public static void setNotificationsEnabledForPackage(Context context, String str, boolean z) {
        setNotificationsEnabledForPackage(context, str, NotificationUtil.getPackageUid(context, str), z);
    }

    public static void setNotificationsEnabledForPackage(Context context, String str, int i, boolean z) {
        try {
            if (XSpaceUserHandle.isUidBelongtoXSpace(i)) {
                setNotificationsEnabledForPackage(str, NotificationUtil.getPackageUid(context.createPackageContextAsUser(str, 2, UserHandle.OWNER), str), z);
            } else if (context.getUserId() == 0 && XSpaceUserHandle.isAppInXSpace(context, str)) {
                setNotificationsEnabledForPackage(str, NotificationUtil.getPackageUid(context.createPackageContextAsUser(str, 2, new UserHandle(999)), str), z);
            }
        } catch (Exception unused) {
        }
        setNotificationsEnabledForPackage(str, i, z);
        try {
            if (!Build.IS_TABLET) {
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
        for (String str2 : stringArray) {
            if (str.equals(str2)) {
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

    public static void setFoldImportance(String str, int i) {
        Context contextForUser = ((UserSwitcherController) Dependency.get(UserSwitcherController.class)).getContextForUser();
        NotificationFilterHelper.setImportance(contextForUser, str, i);
        if (NotificationUtil.isUserOwner(contextForUser)) {
            ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).setFoldImportance(contextForUser, str, i);
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString("package", str);
        bundle.putInt("foldImportance", i);
        try {
            contextForUser.getContentResolver().call(Uri.parse("content://statusbar.notification"), "setFoldImportance", (String) null, bundle);
        } catch (Exception e) {
            Log.e("NotifiSettingsHelper", "Error setFoldImportance " + str, e);
        }
    }

    public static boolean checkFloat(String str, String str2) {
        Context contextForUser = ((UserSwitcherController) Dependency.get(UserSwitcherController.class)).getContextForUser();
        if (NotificationUtil.isUserOwner(contextForUser)) {
            return ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).canFloat(contextForUser, str, str2);
        }
        Bundle bundle = new Bundle();
        bundle.putString("package", str);
        bundle.putString("channel_id", str2);
        try {
            Bundle call = contextForUser.getContentResolver().call(Uri.parse("content://statusbar.notification"), "canFloat", (String) null, bundle);
            if (call != null) {
                return call.getBoolean("canShowFloat");
            }
            return false;
        } catch (Exception e) {
            Log.e("NotifiSettingsHelper", "canFloat " + str, e);
            return false;
        }
    }

    public static boolean checkKeyguard(String str, String str2) {
        Context contextForUser = ((UserSwitcherController) Dependency.get(UserSwitcherController.class)).getContextForUser();
        if (NotificationUtil.isUserOwner(contextForUser)) {
            return ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).canShowOnKeyguard(contextForUser, str, str2);
        }
        Bundle bundle = new Bundle();
        bundle.putString("package", str);
        bundle.putString("channel_id", str2);
        try {
            Bundle call = contextForUser.getContentResolver().call(Uri.parse("content://statusbar.notification"), "canShowOnKeyguard", (String) null, bundle);
            if (call != null) {
                return call.getBoolean("canShowOnKeyguard");
            }
            return false;
        } catch (Exception e) {
            Log.e("NotifiSettingsHelper", "canShowKeyguard " + str, e);
            return false;
        }
    }

    public static boolean checkVibrate(String str, String str2) {
        Context contextForUser = ((UserSwitcherController) Dependency.get(UserSwitcherController.class)).getContextForUser();
        if (NotificationUtil.isUserOwner(contextForUser)) {
            return ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).canVibrate(contextForUser, str);
        }
        Bundle bundle = new Bundle();
        bundle.putString("package", str);
        bundle.putString("channel_id", str2);
        try {
            Bundle call = contextForUser.getContentResolver().call(Uri.parse("content://statusbar.notification"), "canVibrate", (String) null, bundle);
            if (call != null) {
                return call.getBoolean("canVibrate");
            }
            return false;
        } catch (Exception e) {
            Log.e("NotifiSettingsHelper", "canVibrate " + str, e);
            return false;
        }
    }

    public static boolean checkSound(String str, String str2) {
        Context contextForUser = ((UserSwitcherController) Dependency.get(UserSwitcherController.class)).getContextForUser();
        if (NotificationUtil.isUserOwner(contextForUser)) {
            return ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).canSound(contextForUser, str);
        }
        Bundle bundle = new Bundle();
        bundle.putString("package", str);
        bundle.putString("channel_id", str2);
        try {
            Bundle call = contextForUser.getContentResolver().call(Uri.parse("content://statusbar.notification"), "canSound", (String) null, bundle);
            if (call != null) {
                return call.getBoolean("canSound");
            }
            return false;
        } catch (Exception e) {
            Log.e("NotifiSettingsHelper", "canSound " + str, e);
            return false;
        }
    }

    public static boolean checkLights(String str, String str2) {
        Context contextForUser = ((UserSwitcherController) Dependency.get(UserSwitcherController.class)).getContextForUser();
        if (NotificationUtil.isUserOwner(contextForUser)) {
            return ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).canLights(contextForUser, str);
        }
        Bundle bundle = new Bundle();
        bundle.putString("package", str);
        bundle.putString("channel_id", str2);
        try {
            Bundle call = contextForUser.getContentResolver().call(Uri.parse("content://statusbar.notification"), "canLights", (String) null, bundle);
            if (call != null) {
                return call.getBoolean("canLights");
            }
            return false;
        } catch (Exception e) {
            Log.e("NotifiSettingsHelper", "canLights " + str, e);
            return false;
        }
    }

    /* JADX WARNING: Can't wrap try/catch for region: R(6:3|4|5|6|7|9) */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x0037 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean canShowBadge(com.android.systemui.statusbar.notification.ExpandedNotification r7) {
        /*
            java.lang.Class<com.android.systemui.statusbar.policy.UserSwitcherController> r0 = com.android.systemui.statusbar.policy.UserSwitcherController.class
            java.lang.Object r0 = com.android.systemui.Dependency.get(r0)
            com.android.systemui.statusbar.policy.UserSwitcherController r0 = (com.android.systemui.statusbar.policy.UserSwitcherController) r0
            android.content.Context r0 = r0.getContextForUser()
            android.app.Notification r1 = r7.getNotification()
            java.lang.String r1 = r1.getChannelId()
            java.lang.String r2 = "miscellaneous"
            boolean r2 = r2.equals(r1)
            boolean r3 = android.text.TextUtils.isEmpty(r1)
            r4 = 0
            if (r3 != 0) goto L_0x0052
            if (r2 == 0) goto L_0x0024
            goto L_0x0052
        L_0x0024:
            android.app.INotificationManager r2 = com.android.systemui.statusbar.notification.NotificationSettingsHelper.sINM     // Catch:{ Exception -> 0x0037 }
            java.lang.String r3 = r0.getPackageName()     // Catch:{ Exception -> 0x0037 }
            int r5 = r0.getUserId()     // Catch:{ Exception -> 0x0037 }
            java.lang.String r6 = r7.getOpPkg()     // Catch:{ Exception -> 0x0037 }
            android.app.NotificationChannel r4 = r2.getNotificationChannel(r3, r5, r6, r1)     // Catch:{ Exception -> 0x0037 }
            goto L_0x0049
        L_0x0037:
            android.app.INotificationManager r2 = com.android.systemui.statusbar.notification.NotificationSettingsHelper.sINM     // Catch:{ Exception -> 0x0049 }
            java.lang.String r3 = r0.getPackageName()     // Catch:{ Exception -> 0x0049 }
            int r0 = r0.getUserId()     // Catch:{ Exception -> 0x0049 }
            java.lang.String r5 = r7.getPackageName()     // Catch:{ Exception -> 0x0049 }
            android.app.NotificationChannel r4 = r2.getNotificationChannel(r3, r0, r5, r1)     // Catch:{ Exception -> 0x0049 }
        L_0x0049:
            java.lang.String r7 = r7.getPackageName()
            boolean r7 = canShowBadge(r7, r4)
            return r7
        L_0x0052:
            java.lang.String r7 = r7.getPackageName()
            boolean r7 = canShowBadge(r7, r4)
            return r7
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.NotificationSettingsHelper.canShowBadge(com.android.systemui.statusbar.notification.ExpandedNotification):boolean");
    }

    public static boolean canShowBadge(String str, NotificationChannel notificationChannel) {
        boolean z;
        Context contextForUser = ((UserSwitcherController) Dependency.get(UserSwitcherController.class)).getContextForUser();
        if (NotificationUtil.isUserOwner(contextForUser)) {
            z = ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).canShowBadge(contextForUser, str);
        } else {
            Bundle bundle = new Bundle();
            bundle.putString("package", str);
            try {
                Bundle call = contextForUser.getContentResolver().call(Uri.parse("content://statusbar.notification"), "canShowBadge", (String) null, bundle);
                if (call != null) {
                    z = call.getBoolean("canShowBadge");
                }
            } catch (Exception e) {
                Log.e("NotifiSettingsHelper", "canShowBadge " + str, e);
            }
            z = false;
        }
        if (!z) {
            return false;
        }
        if (notificationChannel == null || notificationChannel.canShowBadge()) {
            return true;
        }
        return false;
    }

    public static boolean isSystemApp(String str) {
        return ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).isSystemApp(str);
    }

    public static boolean isPrioritizedApp(String str) {
        return ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).isPrioritizedApp(str);
    }

    public static boolean canSendSubstituteNotification(String str) {
        return ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).canSendSubstituteNotification(str);
    }

    public static boolean disableAutoGroupSummary(String str) {
        return ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).disableAutoGroupSummary(str);
    }

    public static void startAppNotificationSettings(Context context, String str, String str2, int i, String str3) {
        if (DEBUG) {
            Log.d("NotifiSettingsHelper", String.format("startAppNotificationSettings pkg=%s label=%s uid=%s", str, str2, Integer.valueOf(i)));
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
