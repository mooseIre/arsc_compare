package com.android.systemui.statusbar.notification;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiNotificationEntryManager.kt */
final class MiuiNotificationEntryManager$refreshIcons$1 extends Lambda implements Function1<NotificationEntry, Boolean> {
    final /* synthetic */ MiuiNotificationEntryManager this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    MiuiNotificationEntryManager$refreshIcons$1(MiuiNotificationEntryManager miuiNotificationEntryManager) {
        super(1);
        this.this$0 = miuiNotificationEntryManager;
    }

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Boolean invoke(NotificationEntry notificationEntry) {
        return Boolean.valueOf(invoke(notificationEntry));
    }

    public final boolean invoke(@NotNull NotificationEntry notificationEntry) {
        Intrinsics.checkParameterIsNotNull(notificationEntry, "it");
        return this.this$0.isSameUser(notificationEntry.getSbn());
    }
}
