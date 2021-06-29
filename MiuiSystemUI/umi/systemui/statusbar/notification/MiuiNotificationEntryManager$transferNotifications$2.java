package com.android.systemui.statusbar.notification;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiNotificationEntryManager.kt */
final class MiuiNotificationEntryManager$transferNotifications$2 extends Lambda implements Function1<NotificationEntry, Boolean> {
    final /* synthetic */ String $packageName;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    MiuiNotificationEntryManager$transferNotifications$2(String str) {
        super(1);
        this.$packageName = str;
    }

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Boolean invoke(NotificationEntry notificationEntry) {
        return Boolean.valueOf(invoke(notificationEntry));
    }

    public final boolean invoke(@NotNull NotificationEntry notificationEntry) {
        Intrinsics.checkParameterIsNotNull(notificationEntry, "it");
        if (!Intrinsics.areEqual(this.$packageName, "UNIMPORTANT")) {
            ExpandedNotification sbn = notificationEntry.getSbn();
            Intrinsics.checkExpressionValueIsNotNull(sbn, "it.sbn");
            return Intrinsics.areEqual(sbn.getPackageName(), this.$packageName);
        }
    }
}
