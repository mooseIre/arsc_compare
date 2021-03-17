package com.android.systemui.plugins;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import com.android.systemui.plugins.annotations.DependsOn;
import com.android.systemui.plugins.annotations.ProvidesInterface;

@DependsOn(target = NotificationProvider.class)
@ProvidesInterface(action = "com.android.systemui.action.PLUGIN_NOTIFICATION_ASSISTANT", version = 1)
public interface NotificationListenerController extends Plugin {
    public static final String ACTION = "com.android.systemui.action.PLUGIN_NOTIFICATION_ASSISTANT";
    public static final int VERSION = 1;

    @ProvidesInterface(version = 1)
    public interface NotificationProvider {
        public static final int VERSION = 1;

        void addNotification(StatusBarNotification statusBarNotification);

        StatusBarNotification[] getActiveNotifications();

        NotificationListenerService.RankingMap getRankingMap();

        void removeNotification(StatusBarNotification statusBarNotification);

        void updateRanking();
    }

    StatusBarNotification[] getActiveNotifications(StatusBarNotification[] statusBarNotificationArr) {
        return statusBarNotificationArr;
    }

    NotificationListenerService.RankingMap getCurrentRanking(NotificationListenerService.RankingMap rankingMap) {
        return rankingMap;
    }

    void onListenerConnected(NotificationProvider notificationProvider);

    boolean onNotificationPosted(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap) {
        return false;
    }

    boolean onNotificationRemoved(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap) {
        return false;
    }
}
