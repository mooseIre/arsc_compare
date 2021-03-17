package com.android.systemui.statusbar.notification.collection;

import android.view.View;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: NotifViewManager.kt */
public final class NotifViewManager$getListItems$1 extends Lambda implements Function1<Integer, View> {
    final /* synthetic */ SimpleNotificationListContainer $container;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    NotifViewManager$getListItems$1(SimpleNotificationListContainer simpleNotificationListContainer) {
        super(1);
        this.$container = simpleNotificationListContainer;
    }

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ View invoke(Integer num) {
        return invoke(num.intValue());
    }

    @NotNull
    public final View invoke(int i) {
        return this.$container.getContainerChildAt(i);
    }
}
