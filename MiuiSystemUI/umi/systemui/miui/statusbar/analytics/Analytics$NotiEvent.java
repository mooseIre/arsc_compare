package com.android.systemui.miui.statusbar.analytics;

import android.app.Notification;
import android.app.NotificationChannelCompat;
import android.text.TextUtils;
import com.android.systemui.miui.statusbar.ExpandedNotification;
import com.android.systemui.miui.statusbar.notification.NotificationUtil;
import com.xiaomi.stat.MiStatParams;

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

    public static MiStatParams getMiStatParam(ExpandedNotification expandedNotification) {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putString("pkg", NotificationUtil.resoveSendPkg(expandedNotification));
        miStatParams.putString("target_pkg", expandedNotification.getPackageName());
        miStatParams.putLong("ts_id", expandedNotification.getPostTime());
        miStatParams.putString("style", getNotiStyle(expandedNotification.getNotification()));
        miStatParams.putBoolean("clearable", getIsClearableValue(expandedNotification));
        return miStatParams;
    }
}
