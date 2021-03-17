package com.android.systemui.statusbar.notification.collection;

import com.android.systemui.statusbar.notification.stack.NotificationListItem;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.Nullable;

/* compiled from: _Sequences.kt */
public final class NotifViewManager$getListItems$$inlined$filterIsInstance$1 extends Lambda implements Function1<Object, Boolean> {
    public static final NotifViewManager$getListItems$$inlined$filterIsInstance$1 INSTANCE = new NotifViewManager$getListItems$$inlined$filterIsInstance$1();

    public NotifViewManager$getListItems$$inlined$filterIsInstance$1() {
        super(1);
    }

    public final boolean invoke(@Nullable Object obj) {
        return obj instanceof NotificationListItem;
    }
}
