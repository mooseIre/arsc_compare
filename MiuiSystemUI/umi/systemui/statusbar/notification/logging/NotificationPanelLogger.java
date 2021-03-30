package com.android.systemui.statusbar.notification.logging;

import com.android.internal.logging.UiEventLogger;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.logging.nano.Notifications$Notification;
import com.android.systemui.statusbar.notification.logging.nano.Notifications$NotificationList;
import java.util.List;

public interface NotificationPanelLogger {
    static default int toNotificationSection(int i) {
        if (i == 1) {
            return 2;
        }
        if (i == 2) {
            return 1;
        }
        if (i == 3) {
            return 6;
        }
        if (i == 4) {
            return 3;
        }
        if (i != 6) {
            return i != 7 ? 0 : 5;
        }
        return 4;
    }

    void logPanelShown(boolean z, List<NotificationEntry> list);

    public enum NotificationPanelEvent implements UiEventLogger.UiEventEnum {
        NOTIFICATION_PANEL_OPEN_STATUS_BAR(200),
        NOTIFICATION_PANEL_OPEN_LOCKSCREEN(201);
        
        private final int mId;

        private NotificationPanelEvent(int i) {
            this.mId = i;
        }

        public int getId() {
            return this.mId;
        }

        public static NotificationPanelEvent fromLockscreen(boolean z) {
            if (z) {
                return NOTIFICATION_PANEL_OPEN_LOCKSCREEN;
            }
            return NOTIFICATION_PANEL_OPEN_STATUS_BAR;
        }
    }

    static default Notifications$NotificationList toNotificationProto(List<NotificationEntry> list) {
        Notifications$NotificationList notifications$NotificationList = new Notifications$NotificationList();
        if (list == null) {
            return notifications$NotificationList;
        }
        Notifications$Notification[] notifications$NotificationArr = new Notifications$Notification[list.size()];
        int i = 0;
        for (NotificationEntry notificationEntry : list) {
            ExpandedNotification sbn = notificationEntry.getSbn();
            if (sbn != null) {
                Notifications$Notification notifications$Notification = new Notifications$Notification();
                notifications$Notification.uid = sbn.getUid();
                notifications$Notification.packageName = sbn.getPackageName();
                if (sbn.getInstanceId() != null) {
                    notifications$Notification.instanceId = sbn.getInstanceId().getId();
                }
                if (sbn.getNotification() != null) {
                    notifications$Notification.isGroupSummary = sbn.getNotification().isGroupSummary();
                }
                notifications$Notification.section = toNotificationSection(notificationEntry.getBucket());
                notifications$NotificationArr[i] = notifications$Notification;
            }
            i++;
        }
        notifications$NotificationList.notifications = notifications$NotificationArr;
        return notifications$NotificationList;
    }
}
