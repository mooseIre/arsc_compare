package com.android.systemui.statusbar.notification.collection;

import com.android.systemui.statusbar.notification.stack.NotificationListItem;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: NotifViewManager.kt */
public final class NotifViewManager$detachRows$1 extends Lambda implements Function1<NotificationListItem, Boolean> {
    public static final NotifViewManager$detachRows$1 INSTANCE = new NotifViewManager$detachRows$1();

    NotifViewManager$detachRows$1() {
        super(1);
    }

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Boolean invoke(NotificationListItem notificationListItem) {
        return Boolean.valueOf(invoke(notificationListItem));
    }

    public final boolean invoke(@NotNull NotificationListItem notificationListItem) {
        Intrinsics.checkParameterIsNotNull(notificationListItem, "it");
        return !notificationListItem.isBlockingHelperShowing();
    }
}
