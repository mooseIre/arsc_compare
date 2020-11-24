package com.android.systemui.statusbar.notification.logging;

import com.android.systemui.shared.system.SysUiStatsLog;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.logging.NotificationPanelLogger;
import com.android.systemui.statusbar.notification.logging.nano.Notifications$NotificationList;
import com.google.protobuf.nano.MessageNano;
import java.util.List;

public class NotificationPanelLoggerImpl implements NotificationPanelLogger {
    public void logPanelShown(boolean z, List<NotificationEntry> list) {
        Notifications$NotificationList notificationProto = NotificationPanelLogger.toNotificationProto(list);
        SysUiStatsLog.write(245, NotificationPanelLogger.NotificationPanelEvent.fromLockscreen(z).getId(), notificationProto.notifications.length, MessageNano.toByteArray(notificationProto));
    }
}
