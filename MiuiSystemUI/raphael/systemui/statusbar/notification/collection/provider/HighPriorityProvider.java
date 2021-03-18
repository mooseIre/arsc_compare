package com.android.systemui.statusbar.notification.collection.provider;

import android.app.Notification;
import com.android.systemui.statusbar.notification.collection.GroupEntry;
import com.android.systemui.statusbar.notification.collection.ListEntry;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.people.PeopleNotificationIdentifier;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import java.util.List;

public class HighPriorityProvider {
    private final NotificationGroupManager mGroupManager;
    private final PeopleNotificationIdentifier mPeopleNotificationIdentifier;

    public HighPriorityProvider(PeopleNotificationIdentifier peopleNotificationIdentifier, NotificationGroupManager notificationGroupManager) {
        this.mPeopleNotificationIdentifier = peopleNotificationIdentifier;
        this.mGroupManager = notificationGroupManager;
    }

    public boolean isHighPriority(ListEntry listEntry) {
        NotificationEntry representativeEntry;
        if (listEntry == null || (representativeEntry = listEntry.getRepresentativeEntry()) == null) {
            return false;
        }
        if (representativeEntry.getRanking().getImportance() >= 3 || hasHighPriorityCharacteristics(representativeEntry) || hasHighPriorityChild(listEntry)) {
            return true;
        }
        return false;
    }

    private boolean hasHighPriorityChild(ListEntry listEntry) {
        List<NotificationEntry> list;
        if (listEntry instanceof GroupEntry) {
            list = ((GroupEntry) listEntry).getChildren();
        } else {
            list = (listEntry.getRepresentativeEntry() == null || !this.mGroupManager.isGroupSummary(listEntry.getRepresentativeEntry().getSbn())) ? null : this.mGroupManager.getChildren(listEntry.getRepresentativeEntry().getSbn());
        }
        if (list == null) {
            return false;
        }
        for (NotificationEntry notificationEntry : list) {
            if (isHighPriority(notificationEntry)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasHighPriorityCharacteristics(NotificationEntry notificationEntry) {
        return !hasUserSetImportance(notificationEntry) && (isImportantOngoing(notificationEntry) || notificationEntry.getSbn().getNotification().hasMediaSession() || isPeopleNotification(notificationEntry) || isMessagingStyle(notificationEntry));
    }

    private boolean isImportantOngoing(NotificationEntry notificationEntry) {
        return notificationEntry.getSbn().getNotification().isForegroundService() && notificationEntry.getRanking().getImportance() >= 2;
    }

    private boolean isMessagingStyle(NotificationEntry notificationEntry) {
        return Notification.MessagingStyle.class.equals(notificationEntry.getSbn().getNotification().getNotificationStyle());
    }

    private boolean isPeopleNotification(NotificationEntry notificationEntry) {
        return this.mPeopleNotificationIdentifier.getPeopleNotificationType(notificationEntry.getSbn(), notificationEntry.getRanking()) != 0;
    }

    private boolean hasUserSetImportance(NotificationEntry notificationEntry) {
        return notificationEntry.getRanking().getChannel() != null && notificationEntry.getRanking().getChannel().hasUserSetImportance();
    }
}
