package com.android.systemui.util;

import android.app.ActivityManager;
import android.app.ActivityManagerCompat;
import android.app.AppGlobals;
import android.app.Notification;
import android.app.NotificationCompat;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.ArraySet;
import android.util.Pair;
import com.android.systemui.plugins.R;
import java.util.List;

public final class ApplicationInfoHelper {
    public static void postEphemeralNotificationIfNeeded(Context context, String str, int i, ApplicationInfo applicationInfo, NotificationManager notificationManager, int i2, ArraySet<Pair<String, Integer>> arraySet) {
        if (applicationInfo.isInstantApp()) {
            postEphemeralNotif(context, str, i, applicationInfo, notificationManager, i2, arraySet);
        }
    }

    private static void postEphemeralNotif(Context context, String str, int i, ApplicationInfo applicationInfo, NotificationManager notificationManager, int i2, ArraySet<Pair<String, Integer>> arraySet) {
        ComponentName componentName;
        Context context2 = context;
        String str2 = str;
        int i3 = i;
        ApplicationInfo applicationInfo2 = applicationInfo;
        Bundle bundle = new Bundle();
        bundle.putString("android.substName", context2.getString(R.string.instant_apps));
        arraySet.add(new Pair(str2, Integer.valueOf(i)));
        String string = context2.getString(R.string.instant_apps_message);
        PendingIntent activity = PendingIntent.getActivity(context2, 0, new Intent("android.settings.APPLICATION_DETAILS_SETTINGS").setData(Uri.fromParts("package", str2, (String) null)), 0);
        Notification.Action build = new Notification.Action.Builder((Icon) null, context2.getString(R.string.app_info), activity).build();
        Intent taskIntent = getTaskIntent(context2, i2, i3);
        Notification.Builder newBuilder = NotificationCompat.newBuilder(context2, NotificationChannels.GENERAL);
        if (taskIntent != null) {
            taskIntent.setComponent((ComponentName) null).setPackage((String) null).addFlags(512).addFlags(268435456);
            PendingIntent activity2 = PendingIntent.getActivity(context2, 0, taskIntent, 0);
            try {
                componentName = AppGlobals.getPackageManager().getInstantAppInstallerComponent();
            } catch (RemoteException e) {
                e.rethrowFromSystemServer();
                componentName = null;
            }
            newBuilder.addAction(new Notification.Action.Builder((Icon) null, context2.getString(R.string.go_to_web), PendingIntent.getActivity(context2, 0, new Intent().setComponent(componentName).setAction("android.intent.action.VIEW").addCategory("android.intent.category.BROWSABLE").addCategory("unique:" + System.currentTimeMillis()).putExtra("android.intent.extra.PACKAGE_NAME", applicationInfo2.packageName).putExtra("android.intent.extra.VERSION_CODE", applicationInfo2.versionCode).putExtra("android.intent.extra.EPHEMERAL_FAILURE", activity2), 0)).build());
        }
        notificationManager.notifyAsUser(str2, 7, newBuilder.addExtras(bundle).addAction(build).setContentIntent(activity).setColor(context2.getColor(R.color.instant_apps_color)).setContentTitle(applicationInfo2.loadLabel(context.getPackageManager())).setLargeIcon(Icon.createWithResource(str2, applicationInfo2.icon)).setSmallIcon(Icon.createWithResource(context.getPackageName(), R.drawable.instant_icon)).setContentText(string).setOngoing(true).build(), new UserHandle(i3));
    }

    private static Intent getTaskIntent(Context context, int i, int i2) {
        try {
            List<ActivityManager.RecentTaskInfo> recentTasksForUser = ActivityManagerCompat.getRecentTasksForUser((ActivityManager) context.getSystemService(ActivityManager.class), 5, 0, i2);
            for (int i3 = 0; i3 < recentTasksForUser.size(); i3++) {
                if (recentTasksForUser.get(i3).id == i) {
                    return recentTasksForUser.get(i3).baseIntent;
                }
            }
            return null;
        } catch (Exception unused) {
            return null;
        }
    }
}
