package com.android.systemui.statusbar.notification.people;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import org.jetbrains.annotations.NotNull;

/* compiled from: PeopleNotificationIdentifier.kt */
public interface PeopleNotificationIdentifier {
    static {
        Companion companion = Companion.$$INSTANCE;
    }

    int compareTo(int i, int i2);

    int getPeopleNotificationType(@NotNull StatusBarNotification statusBarNotification, @NotNull NotificationListenerService.Ranking ranking);

    /* compiled from: PeopleNotificationIdentifier.kt */
    public static final class Companion {
        static final /* synthetic */ Companion $$INSTANCE = new Companion();

        private Companion() {
        }
    }
}
