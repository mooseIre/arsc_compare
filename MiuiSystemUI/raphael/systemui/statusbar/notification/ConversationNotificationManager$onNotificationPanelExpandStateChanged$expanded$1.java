package com.android.systemui.statusbar.notification;

import com.android.systemui.statusbar.notification.ConversationNotificationManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import java.util.Map;
import kotlin.Pair;
import kotlin.TuplesKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ConversationNotifications.kt */
final class ConversationNotificationManager$onNotificationPanelExpandStateChanged$expanded$1 extends Lambda implements Function1<Map.Entry<? extends String, ? extends ConversationNotificationManager.ConversationState>, Pair<? extends String, ? extends NotificationEntry>> {
    final /* synthetic */ ConversationNotificationManager this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    ConversationNotificationManager$onNotificationPanelExpandStateChanged$expanded$1(ConversationNotificationManager conversationNotificationManager) {
        super(1);
        this.this$0 = conversationNotificationManager;
    }

    @Nullable
    public final Pair<String, NotificationEntry> invoke(@NotNull Map.Entry<String, ConversationNotificationManager.ConversationState> entry) {
        ExpandableNotificationRow row;
        Intrinsics.checkParameterIsNotNull(entry, "<name for destructuring parameter 0>");
        String key = entry.getKey();
        NotificationEntry activeNotificationUnfiltered = this.this$0.notificationEntryManager.getActiveNotificationUnfiltered(key);
        if (activeNotificationUnfiltered == null || (row = activeNotificationUnfiltered.getRow()) == null || !row.isExpanded()) {
            return null;
        }
        return TuplesKt.to(key, activeNotificationUnfiltered);
    }
}
