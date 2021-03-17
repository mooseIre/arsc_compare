package com.android.systemui.statusbar.notification;

import android.app.Notification;
import android.os.Bundle;
import android.text.TextUtils;

public class PushEvents {
    public static String getMessageId(ExpandedNotification expandedNotification) {
        if (expandedNotification.getNotification() == null || expandedNotification.getNotification().extras == null) {
            return null;
        }
        String string = expandedNotification.getNotification().extras.getString("message_id");
        if (!TextUtils.isEmpty(string)) {
            return string;
        }
        long j = expandedNotification.getNotification().extras.getLong("adid");
        return j != 0 ? String.valueOf(j) : string;
    }

    public static String getADId(Notification notification) {
        Bundle bundle;
        if (notification == null || (bundle = notification.extras) == null) {
            return null;
        }
        long j = bundle.getLong("adid");
        if (j != 0) {
            return String.valueOf(j);
        }
        return null;
    }
}
