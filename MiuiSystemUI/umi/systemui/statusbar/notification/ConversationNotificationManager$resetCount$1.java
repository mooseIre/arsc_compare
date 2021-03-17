package com.android.systemui.statusbar.notification;

import com.android.systemui.statusbar.notification.ConversationNotificationManager;
import java.util.function.BiFunction;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* access modifiers changed from: package-private */
/* compiled from: ConversationNotifications.kt */
public final class ConversationNotificationManager$resetCount$1<T, U, R> implements BiFunction<String, ConversationNotificationManager.ConversationState, ConversationNotificationManager.ConversationState> {
    public static final ConversationNotificationManager$resetCount$1 INSTANCE = new ConversationNotificationManager$resetCount$1();

    ConversationNotificationManager$resetCount$1() {
    }

    @Nullable
    public final ConversationNotificationManager.ConversationState apply(@NotNull String str, @Nullable ConversationNotificationManager.ConversationState conversationState) {
        Intrinsics.checkParameterIsNotNull(str, "<anonymous parameter 0>");
        if (conversationState != null) {
            return ConversationNotificationManager.ConversationState.copy$default(conversationState, 0, null, 2, null);
        }
        return null;
    }
}
