package com.android.systemui.statusbar.notification.collection.notifcollection;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;

public interface NotifLifetimeExtender {

    public interface OnEndLifetimeExtensionCallback {
        void onEndLifetimeExtension(NotifLifetimeExtender notifLifetimeExtender, NotificationEntry notificationEntry);
    }

    void cancelLifetimeExtension(NotificationEntry notificationEntry);

    String getName();

    void setCallback(OnEndLifetimeExtensionCallback onEndLifetimeExtensionCallback);

    boolean shouldExtendLifetime(NotificationEntry notificationEntry, int i);
}
