package com.android.systemui.statusbar.policy;

import android.app.PendingIntent;
import android.util.Log;
import com.android.systemui.statusbar.notification.MiuiNotificationCompat;
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
}
