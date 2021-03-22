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
        boolean z = NotificationRankingManagerKt.isColorizedForegroundService(notificationEntry);
        boolean z2 = NotificationRankingManagerKt.isColorizedForegroundService(notificationEntry2);
        int i = this.this$0.getPeopleNotificationType(notificationEntry);
        int i2 = this.this$0.getPeopleNotificationType(notificationEntry2);
        boolean z3 = this.this$0.isImportantMedia(notificationEntry);
        boolean z4 = this.this$0.isImportantMedia(notificationEntry2);
        boolean z5 = NotificationRankingManagerKt.isSystemMax(notificationEntry);
        boolean z6 = NotificationRankingManagerKt.isSystemMax(notificationEntry2);
        boolean isRowHeadsUp = notificationEntry.isRowHeadsUp();
        boolean isRowHeadsUp2 = notificationEntry2.isRowHeadsUp();
        boolean unused = this.this$0.isHighPriority(notificationEntry);
        boolean unused2 = this.this$0.isHighPriority(notificationEntry2);
        if (isRowHeadsUp != isRowHeadsUp2) {
            if (isRowHeadsUp) {
                return -1;
            }
        } else if (isRowHeadsUp) {
            return this.this$0.headsUpManager.compare(notificationEntry, notificationEntry2);
        } else {
            if (z != z2) {
                if (z) {
                    return -1;
                }
            } else if ((this.this$0.getUsePeopleFiltering()) && i != i2) {
                return this.this$0.peopleNotificationIdentifier.compareTo(i, i2);
            } else {
                if (z3 != z4) {
                    if (z3) {
                        return -1;
                    }
                } else if (z5 == z6) {
                    return (sbn2.getNotification().when > sbn.getNotification().when ? 1 : (sbn2.getNotification().when == sbn.getNotification().when ? 0 : -1));
                } else {
                    if (z5) {
                        return -1;
                    }
                }
            }
        }
        return 1;
    }
}
