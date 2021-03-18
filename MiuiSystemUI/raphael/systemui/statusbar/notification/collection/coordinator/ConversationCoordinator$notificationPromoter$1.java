package com.android.systemui.statusbar.notification.collection.coordinator;

import android.app.NotificationChannel;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifPromoter;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ConversationCoordinator.kt */
public final class ConversationCoordinator$notificationPromoter$1 extends NotifPromoter {
    ConversationCoordinator$notificationPromoter$1(String str) {
        super(str);
    }

    @Override // com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifPromoter
    public boolean shouldPromoteToTopLevel(@NotNull NotificationEntry notificationEntry) {
        Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
        NotificationChannel channel = notificationEntry.getChannel();
        return channel != null && channel.isImportantConversation();
    }
}
