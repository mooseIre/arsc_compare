package com.android.systemui.statusbar.notification.collection.inflation;

import com.android.systemui.statusbar.NotificationUiAdjustment;
import com.android.systemui.statusbar.notification.InflationException;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.NotificationRowContentBinder;

public interface NotificationRowBinder {
    void inflateViews(NotificationEntry notificationEntry, Runnable runnable, NotificationRowContentBinder.InflationCallback inflationCallback) throws InflationException;

    void onNotificationRankingUpdated(NotificationEntry notificationEntry, Integer num, NotificationUiAdjustment notificationUiAdjustment, NotificationUiAdjustment notificationUiAdjustment2, NotificationRowContentBinder.InflationCallback inflationCallback);
}
