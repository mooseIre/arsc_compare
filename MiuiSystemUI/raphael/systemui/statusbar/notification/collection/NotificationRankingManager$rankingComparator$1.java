package com.android.systemui.statusbar.notification.collection;

import android.service.notification.NotificationListenerService;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import java.util.Comparator;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: NotificationRankingManager.kt */
final class NotificationRankingManager$rankingComparator$1<T> implements Comparator<NotificationEntry> {
    final /* synthetic */ NotificationRankingManager this$0;

    NotificationRankingManager$rankingComparator$1(NotificationRankingManager notificationRankingManager) {
        this.this$0 = notificationRankingManager;
    }

    public final int compare(NotificationEntry notificationEntry, NotificationEntry notificationEntry2) {
        NotificationEntry notificationEntry3 = notificationEntry;
        NotificationEntry notificationEntry4 = notificationEntry2;
        Intrinsics.checkExpressionValueIsNotNull(notificationEntry3, "a");
        ExpandedNotification sbn = notificationEntry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn, "a.sbn");
        Intrinsics.checkExpressionValueIsNotNull(notificationEntry4, "b");
        ExpandedNotification sbn2 = notificationEntry2.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn2, "b.sbn");
        NotificationListenerService.Ranking ranking = notificationEntry.getRanking();
        Intrinsics.checkExpressionValueIsNotNull(ranking, "a.ranking");
        ranking.getRank();
        NotificationListenerService.Ranking ranking2 = notificationEntry2.getRanking();
        Intrinsics.checkExpressionValueIsNotNull(ranking2, "b.ranking");
        ranking2.getRank();
        boolean access$isColorizedForegroundService = NotificationRankingManagerKt.isColorizedForegroundService(notificationEntry);
        boolean access$isColorizedForegroundService2 = NotificationRankingManagerKt.isColorizedForegroundService(notificationEntry2);
        int access$getPeopleNotificationType = this.this$0.getPeopleNotificationType(notificationEntry3);
        int access$getPeopleNotificationType2 = this.this$0.getPeopleNotificationType(notificationEntry4);
        boolean access$isImportantMedia = this.this$0.isImportantMedia(notificationEntry3);
        boolean access$isImportantMedia2 = this.this$0.isImportantMedia(notificationEntry4);
        boolean access$isSystemMax = NotificationRankingManagerKt.isSystemMax(notificationEntry);
        boolean access$isSystemMax2 = NotificationRankingManagerKt.isSystemMax(notificationEntry2);
        boolean isRowHeadsUp = notificationEntry.isRowHeadsUp();
        boolean isRowHeadsUp2 = notificationEntry2.isRowHeadsUp();
        boolean unused = this.this$0.isHighPriority(notificationEntry3);
        boolean unused2 = this.this$0.isHighPriority(notificationEntry4);
        if (isRowHeadsUp != isRowHeadsUp2) {
            if (isRowHeadsUp) {
                return -1;
            }
        } else if (isRowHeadsUp) {
            return this.this$0.headsUpManager.compare(notificationEntry3, notificationEntry4);
        } else {
            if (access$isColorizedForegroundService != access$isColorizedForegroundService2) {
                if (access$isColorizedForegroundService) {
                    return -1;
                }
            } else if (this.this$0.getUsePeopleFiltering() && access$getPeopleNotificationType != access$getPeopleNotificationType2) {
                return this.this$0.peopleNotificationIdentifier.compareTo(access$getPeopleNotificationType, access$getPeopleNotificationType2);
            } else {
                if (access$isImportantMedia != access$isImportantMedia2) {
                    if (access$isImportantMedia) {
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
