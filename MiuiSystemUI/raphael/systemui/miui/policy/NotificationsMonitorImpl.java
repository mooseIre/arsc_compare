package com.android.systemui.miui.policy;

import android.service.notification.StatusBarNotification;
import com.android.systemui.miui.policy.NotificationsMonitor;
import java.util.ArrayList;
import java.util.List;

public class NotificationsMonitorImpl implements NotificationsMonitor {
    private final List<NotificationsMonitor.Callback> mCallbacks = new ArrayList();

    public void notifyNotificationAdded(StatusBarNotification statusBarNotification) {
        for (NotificationsMonitor.Callback onNotificationAdded : new ArrayList(this.mCallbacks)) {
            onNotificationAdded.onNotificationAdded(statusBarNotification);
        }
    }

    public void notifyNotificationArrived(StatusBarNotification statusBarNotification) {
        for (NotificationsMonitor.Callback onNotificationArrived : new ArrayList(this.mCallbacks)) {
            onNotificationArrived.onNotificationArrived(statusBarNotification);
        }
    }

    public void notifyNotificationUpdated(StatusBarNotification statusBarNotification) {
        for (NotificationsMonitor.Callback onNotificationUpdated : new ArrayList(this.mCallbacks)) {
            onNotificationUpdated.onNotificationUpdated(statusBarNotification);
        }
    }

    public void addCallback(NotificationsMonitor.Callback callback) {
        synchronized (this.mCallbacks) {
            this.mCallbacks.add(callback);
        }
    }

    public void removeCallback(NotificationsMonitor.Callback callback) {
        synchronized (this.mCallbacks) {
            this.mCallbacks.remove(callback);
        }
    }
}
