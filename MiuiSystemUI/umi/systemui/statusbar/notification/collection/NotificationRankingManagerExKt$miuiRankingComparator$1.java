package com.android.systemui.statusbar.notification.collection;

import com.android.systemui.statusbar.notification.ExpandedNotification;
import java.util.Comparator;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: NotificationRankingManagerEx.kt */
final class NotificationRankingManagerExKt$miuiRankingComparator$1<T> implements Comparator<NotificationEntry> {
    public static final NotificationRankingManagerExKt$miuiRankingComparator$1 INSTANCE = new NotificationRankingManagerExKt$miuiRankingComparator$1();

    NotificationRankingManagerExKt$miuiRankingComparator$1() {
    }

    public final int compare(NotificationEntry notificationEntry, NotificationEntry notificationEntry2) {
        Intrinsics.checkExpressionValueIsNotNull(notificationEntry, "a");
        ExpandedNotification sbn = notificationEntry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn, "a.sbn");
        Intrinsics.checkExpressionValueIsNotNull(notificationEntry2, "b");
        ExpandedNotification sbn2 = notificationEntry2.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn2, "b.sbn");
        boolean isShowingAtTail = sbn.isShowingAtTail();
        boolean isShowingAtTail2 = sbn2.isShowingAtTail();
        boolean isSystemWarnings = sbn.isSystemWarnings();
        boolean isSystemWarnings2 = sbn.isSystemWarnings();
        if (isShowingAtTail != isShowingAtTail2) {
            if (isShowingAtTail2) {
                return -1;
            }
        } else if (isSystemWarnings == isSystemWarnings2) {
            return 0;
        } else {
            if (isSystemWarnings) {
                return -1;
            }
        }
        return 1;
    }
}
