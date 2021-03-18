package com.android.systemui.statusbar.notification;

import com.android.systemui.statusbar.notification.ConversationNotificationManager;
import java.util.Map;
import java.util.function.BiFunction;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: ConversationNotifications.kt */
public final class ConversationNotificationManager$onNotificationPanelExpandStateChanged$1<T, U, R> implements BiFunction<String, ConversationNotificationManager.ConversationState, ConversationNotificationManager.ConversationState> {
    final /* synthetic */ Map $expanded;

    ConversationNotificationManager$onNotificationPanelExpandStateChanged$1(Map map) {
        this.$expanded = map;
    }

    @NotNull
    public final ConversationNotificationManager.ConversationState apply(@NotNull String str, @NotNull ConversationNotificationManager.ConversationState conversationState) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        Intrinsics.checkParameterIsNotNull(conversationState, "state");
        return this.$expanded.containsKey(str) ? ConversationNotificationManager.ConversationState.copy$default(conversationState, 0, null, 2, null) : conversationState;
    }
}
