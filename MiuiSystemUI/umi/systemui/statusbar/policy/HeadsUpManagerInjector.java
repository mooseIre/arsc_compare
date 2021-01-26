package com.android.systemui.statusbar.policy;

import android.app.PendingIntent;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.android.systemui.C0021R$string;
import com.android.systemui.statusbar.notification.MiuiNotificationCompat;
import com.android.systemui.statusbar.notification.NotificationUtil;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;

public class HeadsUpManagerInjector {
    private static boolean sSnoozeNotify = false;

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

    public static boolean injectSnooze(Context context, NotificationEntry notificationEntry) {
        if (NotificationUtil.isInCallNotification(notificationEntry.getSbn())) {
            return true;
        }
        if (sSnoozeNotify) {
            return false;
        }
        Toast.makeText(context, context.getText(C0021R$string.heads_up_snooze_toast), 1).show();
        sSnoozeNotify = true;
        return false;
    }

    public static int getMiuiFloatTime(NotificationEntry notificationEntry) {
        int floatTime;
        if (notificationEntry == null || (floatTime = notificationEntry.getSbn().getFloatTime()) <= 0) {
            return 0;
        }
        return floatTime;
    }

    public static boolean getSnoozeNotify() {
        return sSnoozeNotify;
    }
}
