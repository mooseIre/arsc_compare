package com.android.systemui.statusbar.notification.collection.inflation;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;

public interface NotifInflater {

    public interface InflationCallback {
        void onInflationFinished(NotificationEntry notificationEntry);
    }

    void inflateViews(NotificationEntry notificationEntry, InflationCallback inflationCallback);

    void rebindViews(NotificationEntry notificationEntry, InflationCallback inflationCallback);
}
