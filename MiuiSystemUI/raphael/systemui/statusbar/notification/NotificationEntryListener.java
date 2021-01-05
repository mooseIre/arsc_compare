package com.android.systemui.statusbar.notification;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;

public interface NotificationEntryListener {
    void onEntryInflated(NotificationEntry notificationEntry) {
    }

    void onEntryReinflated(NotificationEntry notificationEntry) {
    }

    void onEntryRemoved(NotificationEntry notificationEntry, NotificationVisibility notificationVisibility, boolean z, int i) {
    }

    void onInflationError(StatusBarNotification statusBarNotification, Exception exc) {
    }

    void onNotificationAdded(NotificationEntry notificationEntry) {
    }

    void onNotificationRankingUpdated(NotificationListenerService.RankingMap rankingMap) {
    }

    void onPendingEntryAdded(NotificationEntry notificationEntry) {
    }

    void onPostEntryUpdated(NotificationEntry notificationEntry) {
    }

    void onPreEntryUpdated(NotificationEntry notificationEntry) {
    }
}
