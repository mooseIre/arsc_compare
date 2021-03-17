package com.android.systemui.statusbar.notification.collection.notifcollection;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;

public interface NotifCollectionListener {
    default void onEntryAdded(NotificationEntry notificationEntry) {
    }

    default void onEntryBind(NotificationEntry notificationEntry, StatusBarNotification statusBarNotification) {
    }

    default void onEntryCleanUp(NotificationEntry notificationEntry) {
    }

    default void onEntryInit(NotificationEntry notificationEntry) {
    }

    default void onEntryRemoved(NotificationEntry notificationEntry, int i) {
    }

    default void onEntryUpdated(NotificationEntry notificationEntry) {
    }

    default void onRankingApplied() {
    }

    default void onRankingUpdate(NotificationListenerService.RankingMap rankingMap) {
    }
}
