package com.android.systemui.statusbar.notification;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.unimportant.FoldTool;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiNotificationEntryManager.kt */
final class MiuiNotificationEntryManager$transferNotifications$1 extends Lambda implements Function1<NotificationEntry, Boolean> {
    public static final MiuiNotificationEntryManager$transferNotifications$1 INSTANCE = new MiuiNotificationEntryManager$transferNotifications$1();

    MiuiNotificationEntryManager$transferNotifications$1() {
        super(1);
    }

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Boolean invoke(NotificationEntry notificationEntry) {
        return Boolean.valueOf(invoke(notificationEntry));
    }

    public final boolean invoke(@NotNull NotificationEntry notificationEntry) {
        Intrinsics.checkParameterIsNotNull(notificationEntry, "it");
        return FoldTool.isSameUser$default(FoldTool.INSTANCE, notificationEntry.getSbn(), 0, 2, null);
    }
}
