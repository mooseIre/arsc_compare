package com.android.systemui.miui.statusbar.analytics;

import android.app.Notification;
import android.app.NotificationChannelCompat;
import android.text.TextUtils;
import com.android.systemui.miui.statusbar.ExpandedNotification;
import com.android.systemui.miui.statusbar.notification.NotificationUtil;
import java.util.HashMap;
import java.util.Map;

public class Analytics$NotiEvent extends Analytics$Event {
    public static String getSource(boolean z, boolean z2) {
        return z ? "float" : z2 ? "keyguard" : "panel";
    }

    public static String getNotiStyle(Notification notification) {
        String string = notification.extras.getString("android.template");
        if (TextUtils.isEmpty(string)) {
            return "Normal";
        }
        int lastIndexOf = string.lastIndexOf("$");
        return lastIndexOf > 0 ? string.substring(lastIndexOf + 1) : "Unknown";
    }

    public static boolean getIsClearableValue(ExpandedNotification expandedNotification) {
        return expandedNotification.isClearable();
    }

    public static String getChannelValue(NotificationChannelCompat notificationChannelCompat) {
        return (notificationChannelCompat == null || "miscellaneous".equals(notificationChannelCompat.getId())) ? "" : notificationChannelCompat.getId();
    }

    public static Map<String, Object> getStatParam(ExpandedNotification expandedNotification) {
        HashMap hashMap = new HashMap();
        hashMap.put("pkg", NotificationUtil.resoveSendPkg(expandedNotification));
        hashMap.put("target_pkg", expandedNotification.getPackageName());
        hashMap.put("ts_id", Long.valueOf(expandedNotification.getPostTime()));
        hashMap.put("style", getNotiStyle(expandedNotification.getNotification()));
        hashMap.put("clearable", Boolean.valueOf(getIsClearableValue(expandedNotification)));
        return hashMap;
    }
}
