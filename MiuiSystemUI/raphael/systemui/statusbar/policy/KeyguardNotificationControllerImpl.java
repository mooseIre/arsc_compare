package com.android.systemui.statusbar.policy;

public class KeyguardNotificationControllerImpl implements KeyguardNotificationController {
    private NotificationChangeListener mNotificationListener;

    public void update(String str) {
        NotificationChangeListener notificationChangeListener = this.mNotificationListener;
        if (notificationChangeListener != null) {
            notificationChangeListener.onUpdate(str);
        }
    }

    public void clearAll() {
        NotificationChangeListener notificationChangeListener = this.mNotificationListener;
        if (notificationChangeListener != null) {
            notificationChangeListener.onClearAll();
        }
    }

    public void add(String str) {
        NotificationChangeListener notificationChangeListener = this.mNotificationListener;
        if (notificationChangeListener != null) {
            notificationChangeListener.onAdd(str);
        }
    }

    public void delete(String str) {
        NotificationChangeListener notificationChangeListener = this.mNotificationListener;
        if (notificationChangeListener != null) {
            notificationChangeListener.onDelete(str);
        }
    }
}
