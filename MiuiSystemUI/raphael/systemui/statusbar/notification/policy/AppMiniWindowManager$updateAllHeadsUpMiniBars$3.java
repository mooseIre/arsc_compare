package com.android.systemui.statusbar.notification.policy;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.MiuiExpandableNotificationRow;
import java.util.function.Function;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: AppMiniWindowManager.kt */
public final class AppMiniWindowManager$updateAllHeadsUpMiniBars$3<T, R> implements Function<T, R> {
    public static final AppMiniWindowManager$updateAllHeadsUpMiniBars$3 INSTANCE = new AppMiniWindowManager$updateAllHeadsUpMiniBars$3();

    AppMiniWindowManager$updateAllHeadsUpMiniBars$3() {
    }

    @NotNull
    public final MiuiExpandableNotificationRow apply(NotificationEntry notificationEntry) {
        Intrinsics.checkExpressionValueIsNotNull(notificationEntry, "it");
        ExpandableNotificationRow row = notificationEntry.getRow();
        if (row != null) {
            return (MiuiExpandableNotificationRow) row;
        }
        throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.statusbar.notification.row.MiuiExpandableNotificationRow");
    }
}
