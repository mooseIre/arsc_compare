package com.android.systemui.statusbar.notification;

import com.android.systemui.statusbar.notification.ConversationNotificationManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* compiled from: ConversationNotifications.kt */
final class ConversationNotificationManager$1$onNotificationRankingUpdated$activeConversationEntries$1 extends Lambda implements Function1<String, NotificationEntry> {
    final /* synthetic */ ConversationNotificationManager.AnonymousClass1 this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    ConversationNotificationManager$1$onNotificationRankingUpdated$activeConversationEntries$1(ConversationNotificationManager.AnonymousClass1 r1) {
        super(1);
        this.this$0 = r1;
    }

    public final NotificationEntry invoke(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "it");
        return this.this$0.this$0.notificationEntryManager.getActiveNotificationUnfiltered(str);
    }
}
