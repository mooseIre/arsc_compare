package com.android.systemui.statusbar.notification.collection;

import android.view.View;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* compiled from: NotifViewManager.kt */
final class NotifViewManager$getListItems$1 extends Lambda implements Function1<Integer, View> {
    final /* synthetic */ SimpleNotificationListContainer $container;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    NotifViewManager$getListItems$1(SimpleNotificationListContainer simpleNotificationListContainer) {
        super(1);
        this.$container = simpleNotificationListContainer;
    }

    public /* bridge */ /* synthetic */ Object invoke(Object obj) {
        return invoke(((Number) obj).intValue());
    }

    @NotNull
    public final View invoke(int i) {
        return this.$container.getContainerChildAt(i);
    }
}
