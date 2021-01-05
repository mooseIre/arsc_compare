package com.android.systemui.statusbar.policy;

import android.app.PendingIntent;
import android.util.Log;
import com.android.systemui.statusbar.notification.MiuiNotificationCompat;
import com.android.systemui.statusbar.notification.NotificationUtil;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;

public class HeadsUpManagerInjector {
    public static void sendExitFloatingIntent(NotificationEntry notificationEntry) {
        PendingIntent exitFloatingIntent = MiuiNotificationCompat.getExitFloatingIntent(notificationEntry.getSbn().getNotification());
        if (exitFloatingIntent != null) {
            try {
                exitFloatingIntent.send();
            } catch (Exception e) {
                Log.d("HeadsUpManagerInjector", "sendExitFloatingIntent " + notificationEntry.getKey(), e);
            }
        }
    }

    public static boolean skipSnooze(NotificationEntry notificationEntry) {
        return NotificationUtil.isInCallNotification(notificationEntry.getSbn());
    }

    public static int getMiuiFloatTime(NotificationEntry notificationEntry) {
        int floatTime;
        if (notificationEntry == null || (floatTime = notificationEntry.getSbn().getFloatTime()) <= 0) {
            return 0;
        }
        return floatTime;
    }
}
