package com.android.systemui.statusbar.notification;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: ConversationNotifications.kt */
public final class ConversationNotificationManager$onNotificationPanelExpandStateChanged$2 extends Lambda implements Function1<NotificationEntry, ExpandableNotificationRow> {
    public static final ConversationNotificationManager$onNotificationPanelExpandStateChanged$2 INSTANCE = new ConversationNotificationManager$onNotificationPanelExpandStateChanged$2();

    ConversationNotificationManager$onNotificationPanelExpandStateChanged$2() {
        super(1);
    }

    public final ExpandableNotificationRow invoke(@NotNull NotificationEntry notificationEntry) {
        Intrinsics.checkParameterIsNotNull(notificationEntry, "it");
        return notificationEntry.getRow();
    }
}
