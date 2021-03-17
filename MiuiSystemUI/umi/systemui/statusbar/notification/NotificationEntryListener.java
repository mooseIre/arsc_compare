package com.android.systemui.statusbar.notification;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;

public interface NotificationEntryListener {
    default void onEntryInflated(NotificationEntry notificationEntry) {
    }

    default void onEntryReinflated(NotificationEntry notificationEntry) {
    }

    default void onEntryRemoved(NotificationEntry notificationEntry, NotificationVisibility notificationVisibility, boolean z, int i) {
    }

    default void onInflationError(StatusBarNotification statusBarNotification, Exception exc) {
    }

    default void onNotificationAdded(NotificationEntry notificationEntry) {
    }

    default void onNotificationRankingUpdated(NotificationListenerService.RankingMap rankingMap) {
    }

    default void onPendingEntryAdded(NotificationEntry notificationEntry) {
    }

    default void onPostEntryUpdated(NotificationEntry notificationEntry) {
    }

    default void onPreEntryUpdated(NotificationEntry notificationEntry) {
    }
}
