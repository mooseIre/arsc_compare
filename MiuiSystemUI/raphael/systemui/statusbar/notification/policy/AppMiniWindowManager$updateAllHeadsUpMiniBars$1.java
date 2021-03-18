package com.android.systemui.statusbar.notification.policy;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import java.util.function.Predicate;
import kotlin.jvm.internal.Intrinsics;

/* access modifiers changed from: package-private */
/* compiled from: AppMiniWindowManager.kt */
public final class AppMiniWindowManager$updateAllHeadsUpMiniBars$1<T> implements Predicate<NotificationEntry> {
    public static final AppMiniWindowManager$updateAllHeadsUpMiniBars$1 INSTANCE = new AppMiniWindowManager$updateAllHeadsUpMiniBars$1();

    AppMiniWindowManager$updateAllHeadsUpMiniBars$1() {
    }

    public final boolean test(NotificationEntry notificationEntry) {
        Intrinsics.checkExpressionValueIsNotNull(notificationEntry, "it");
        return notificationEntry.isRowPinned();
    }
}
