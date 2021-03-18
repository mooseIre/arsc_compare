package com.android.systemui.statusbar.notification.policy;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.MiuiExpandableNotificationRow;
import java.util.function.Predicate;
import kotlin.jvm.internal.Intrinsics;

/* access modifiers changed from: package-private */
/* compiled from: AppMiniWindowManager.kt */
public final class AppMiniWindowManager$updateAllHeadsUpMiniBars$2<T> implements Predicate<NotificationEntry> {
    public static final AppMiniWindowManager$updateAllHeadsUpMiniBars$2 INSTANCE = new AppMiniWindowManager$updateAllHeadsUpMiniBars$2();

    AppMiniWindowManager$updateAllHeadsUpMiniBars$2() {
    }

    public final boolean test(NotificationEntry notificationEntry) {
        Intrinsics.checkExpressionValueIsNotNull(notificationEntry, "it");
        return notificationEntry.getRow() instanceof MiuiExpandableNotificationRow;
    }
}
