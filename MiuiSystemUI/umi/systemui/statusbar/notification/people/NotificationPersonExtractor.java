package com.android.systemui.statusbar.notification.people;

import android.service.notification.StatusBarNotification;
import org.jetbrains.annotations.NotNull;

/* compiled from: PeopleHubNotificationListener.kt */
public interface NotificationPersonExtractor {
    boolean isPersonNotification(@NotNull StatusBarNotification statusBarNotification);
}
