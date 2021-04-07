package com.android.systemui.statusbar.notification.collection;

import android.service.notification.NotificationListenerService;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import java.util.Comparator;
import kotlin.jvm.internal.Intrinsics;

/* access modifiers changed from: package-private */
/* compiled from: NotificationRankingManager.kt */
public final class NotificationRankingManager$rankingComparator$1<T> implements Comparator<NotificationEntry> {
    final /* synthetic */ NotificationRankingManager this$0;

    NotificationRankingManager$rankingComparator$1(NotificationRankingManager notificationRankingManager) {
        this.this$0 = notificationRankingManager;
    }

    public final int compare(NotificationEntry notificationEntry, NotificationEntry notificationEntry2) {
        Intrinsics.checkExpressionValueIsNotNull(notificationEntry, "a");
        ExpandedNotification sbn = notificationEntry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn, "a.sbn");
        Intrinsics.checkExpressionValueIsNotNull(notificationEntry2, "b");
        ExpandedNotification sbn2 = notificationEntry2.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn2, "b.sbn");
        NotificationListenerService.Ranking ranking = notificationEntry.getRanking();
        Intrinsics.checkExpressionValueIsNotNull(ranking, "a.ranking");
        ranking.getRank();
        NotificationListenerService.Ranking ranking2 = notificationEntry2.getRanking();
        Intrinsics.checkExpressionValueIsNotNull(ranking2, "b.ranking");
        ranking2.getRank();
        boolean access$isColorizedForegroundService = NotificationRankingManagerKt.access$isColorizedForegroundService(notificationEntry);
        boolean access$isColorizedForegroundService2 = NotificationRankingManagerKt.access$isColorizedForegroundService(notificationEntry2);
        int i = this.this$0.getPeopleNotificationType(notificationEntry);
        int i2 = this.this$0.getPeopleNotificationType(notificationEntry2);
        boolean z = this.this$0.isImportantMedia(notificationEntry);
        boolean z2 = this.this$0.isImportantMedia(notificationEntry2);
        boolean access$isSystemMax = NotificationRankingManagerKt.access$isSystemMax(notificationEntry);
        boolean access$isSystemMax2 = NotificationRankingManagerKt.access$isSystemMax(notificationEntry2);
        boolean isRowHeadsUp = notificationEntry.isRowHeadsUp();
        boolean isRowHeadsUp2 = notificationEntry2.isRowHeadsUp();
        boolean unused = this.this$0.isHighPriority(notificationEntry);
        boolean unused2 = this.this$0.isHighPriority(notificationEntry2);
        ExpandedNotification sbn3 = notificationEntry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn3, "a.sbn");
        boolean isImportant = sbn3.isImportant();
        ExpandedNotification sbn4 = notificationEntry2.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn4, "b.sbn");
        boolean isImportant2 = sbn4.isImportant();
        if (isRowHeadsUp != isRowHeadsUp2) {
            if (isRowHeadsUp) {
                return -1;
            }
        } else if (isRowHeadsUp) {
            return this.this$0.headsUpManager.compare(notificationEntry, notificationEntry2);
        } else {
            if (access$isColorizedForegroundService != access$isColorizedForegroundService2) {
                if (access$isColorizedForegroundService) {
                    return -1;
                }
            } else if ((this.this$0.getUsePeopleFiltering()) && i != i2) {
                return this.this$0.peopleNotificationIdentifier.compareTo(i, i2);
            } else {
                if (isImportant != isImportant2) {
                    if (isImportant) {
                        return -1;
                    }
                } else if (z != z2) {
                    if (z) {
                        return -1;
                    }
                } else if (access$isSystemMax == access$isSystemMax2) {
                    return (sbn2.getNotification().when > sbn.getNotification().when ? 1 : (sbn2.getNotification().when == sbn.getNotification().when ? 0 : -1));
                } else {
                    if (access$isSystemMax) {
                        return -1;
                    }
                }
            }
        }
        return 1;
    }
}
