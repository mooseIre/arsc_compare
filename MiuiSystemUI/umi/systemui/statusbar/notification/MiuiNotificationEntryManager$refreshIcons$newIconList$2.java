package com.android.systemui.statusbar.notification;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiNotificationEntryManager.kt */
final class MiuiNotificationEntryManager$refreshIcons$newIconList$2 extends Lambda implements Function1<NotificationEntry, String> {
    public static final MiuiNotificationEntryManager$refreshIcons$newIconList$2 INSTANCE = new MiuiNotificationEntryManager$refreshIcons$newIconList$2();

    MiuiNotificationEntryManager$refreshIcons$newIconList$2() {
        super(1);
    }

    public final String invoke(@NotNull NotificationEntry notificationEntry) {
        Intrinsics.checkParameterIsNotNull(notificationEntry, "it");
        ExpandedNotification sbn = notificationEntry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn, "it.sbn");
        return sbn.getPackageName();
    }
}
