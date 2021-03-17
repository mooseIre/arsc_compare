package com.android.systemui.statusbar.notification.collection.notifcollection;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;

public interface NotifCollectionListener {
    void onEntryAdded(NotificationEntry notificationEntry) {
    }

    void onEntryBind(NotificationEntry notificationEntry, StatusBarNotification statusBarNotification) {
    }

    void onEntryCleanUp(NotificationEntry notificationEntry) {
    }

    void onEntryInit(NotificationEntry notificationEntry) {
    }

    void onEntryRemoved(NotificationEntry notificationEntry, int i) {
    }

    void onEntryUpdated(NotificationEntry notificationEntry) {
    }

    void onRankingApplied() {
    }

    void onRankingUpdate(NotificationListenerService.RankingMap rankingMap) {
    }
}
