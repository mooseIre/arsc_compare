package com.android.systemui;

import android.service.notification.StatusBarNotification;

public interface ForegroundServiceController {
    void addNotification(StatusBarNotification statusBarNotification, int i);

    boolean isDungeonNeededForUser(int i);

    boolean isDungeonNotification(StatusBarNotification statusBarNotification);

    boolean removeNotification(StatusBarNotification statusBarNotification);

    void updateNotification(StatusBarNotification statusBarNotification, int i);
}
