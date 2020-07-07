package com.android.systemui.miui.policy;

import android.service.notification.StatusBarNotification;
import com.android.systemui.statusbar.policy.CallbackController;

public interface NotificationsMonitor extends CallbackController<Callback> {

    public interface Callback {
        void onNotificationAdded(StatusBarNotification statusBarNotification);

        void onNotificationArrived(StatusBarNotification statusBarNotification);

        void onNotificationUpdated(StatusBarNotification statusBarNotification);
    }

    void notifyNotificationAdded(StatusBarNotification statusBarNotification);

    void notifyNotificationArrived(StatusBarNotification statusBarNotification);

    void notifyNotificationUpdated(StatusBarNotification statusBarNotification);
}
