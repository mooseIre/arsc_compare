package com.android.systemui.statusbar.notification;

import android.app.Notification;
import com.android.systemui.statusbar.notification.ConversationNotificationManager;
import java.util.function.BiFunction;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ConversationNotifications.kt */
final class ConversationNotificationManager$resetCount$1<T, U, R> implements BiFunction<String, ConversationNotificationManager.ConversationState, ConversationNotificationManager.ConversationState> {
    public static final ConversationNotificationManager$resetCount$1 INSTANCE = new ConversationNotificationManager$resetCount$1();

    ConversationNotificationManager$resetCount$1() {
    }

    @Nullable
    public final ConversationNotificationManager.ConversationState apply(@NotNull String str, @Nullable ConversationNotificationManager.ConversationState conversationState) {
        Intrinsics.checkParameterIsNotNull(str, "<anonymous parameter 0>");
        if (conversationState != null) {
            return ConversationNotificationManager.ConversationState.copy$default(conversationState, 0, (Notification) null, 2, (Object) null);
        }
        return null;
    }
}
