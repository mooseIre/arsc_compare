package com.android.systemui.statusbar.notification;

import android.graphics.drawable.Drawable;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiNotificationEntryManager.kt */
final class MiuiNotificationEntryManager$refreshIcons$3 extends Lambda implements Function1<NotificationEntry, Drawable> {
    public static final MiuiNotificationEntryManager$refreshIcons$3 INSTANCE = new MiuiNotificationEntryManager$refreshIcons$3();

    MiuiNotificationEntryManager$refreshIcons$3() {
        super(1);
    }

    public final Drawable invoke(@NotNull NotificationEntry notificationEntry) {
        Intrinsics.checkParameterIsNotNull(notificationEntry, "it");
        ExpandedNotification sbn = notificationEntry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn, "it.sbn");
        return sbn.getAppIcon();
    }
}
