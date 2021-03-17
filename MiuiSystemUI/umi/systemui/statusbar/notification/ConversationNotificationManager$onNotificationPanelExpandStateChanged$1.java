package com.android.systemui.statusbar.notification;

import android.app.Notification;
import com.android.systemui.statusbar.notification.ConversationNotificationManager;
import java.util.Map;
import java.util.function.BiFunction;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ConversationNotifications.kt */
final class ConversationNotificationManager$onNotificationPanelExpandStateChanged$1<T, U, R> implements BiFunction<String, ConversationNotificationManager.ConversationState, ConversationNotificationManager.ConversationState> {
    final /* synthetic */ Map $expanded;

    ConversationNotificationManager$onNotificationPanelExpandStateChanged$1(Map map) {
        this.$expanded = map;
    }

    @NotNull
    public final ConversationNotificationManager.ConversationState apply(@NotNull String str, @NotNull ConversationNotificationManager.ConversationState conversationState) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        Intrinsics.checkParameterIsNotNull(conversationState, "state");
        return this.$expanded.containsKey(str) ? ConversationNotificationManager.ConversationState.copy$default(conversationState, 0, (Notification) null, 2, (Object) null) : conversationState;
    }
}
