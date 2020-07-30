package com.android.systemui.statusbar.notification;

import android.service.notification.StatusBarNotification;
import com.android.systemui.statusbar.ExpandableNotificationRow;

public interface NotificationActivityStarter {
    void onNotificationClicked(StatusBarNotification statusBarNotification, ExpandableNotificationRow expandableNotificationRow);
}
