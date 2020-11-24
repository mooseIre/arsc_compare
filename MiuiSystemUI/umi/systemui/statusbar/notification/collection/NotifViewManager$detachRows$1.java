package com.android.systemui.statusbar.notification.collection;

import com.android.systemui.statusbar.notification.stack.NotificationListItem;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* compiled from: NotifViewManager.kt */
final class NotifViewManager$detachRows$1 extends Lambda implements Function1<NotificationListItem, Boolean> {
    public static final NotifViewManager$detachRows$1 INSTANCE = new NotifViewManager$detachRows$1();

    NotifViewManager$detachRows$1() {
        super(1);
    }

    public /* bridge */ /* synthetic */ Object invoke(Object obj) {
        return Boolean.valueOf(invoke((NotificationListItem) obj));
    }

    public final boolean invoke(@NotNull NotificationListItem notificationListItem) {
        Intrinsics.checkParameterIsNotNull(notificationListItem, "it");
        return !notificationListItem.isBlockingHelperShowing();
    }
}
