package com.android.systemui.statusbar.notification;

import android.app.Notification;
import com.android.systemui.statusbar.notification.ConversationNotificationManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import java.util.function.BiFunction;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* access modifiers changed from: package-private */
/* compiled from: ConversationNotifications.kt */
public final class ConversationNotificationManager$getUnreadCount$1<T, U, R> implements BiFunction<String, ConversationNotificationManager.ConversationState, ConversationNotificationManager.ConversationState> {
    final /* synthetic */ NotificationEntry $entry;
    final /* synthetic */ Notification.Builder $recoveredBuilder;
    final /* synthetic */ ConversationNotificationManager this$0;

    ConversationNotificationManager$getUnreadCount$1(ConversationNotificationManager conversationNotificationManager, Notification.Builder builder, NotificationEntry notificationEntry) {
        this.this$0 = conversationNotificationManager;
        this.$recoveredBuilder = builder;
        this.$entry = notificationEntry;
    }

    @NotNull
    public final ConversationNotificationManager.ConversationState apply(@NotNull String str, @Nullable ConversationNotificationManager.ConversationState conversationState) {
        Intrinsics.checkParameterIsNotNull(str, "<anonymous parameter 0>");
        int i = 1;
        if (conversationState != null) {
            i = Notification.areStyledNotificationsVisiblyDifferent(Notification.Builder.recoverBuilder(this.this$0.context, conversationState.getNotification()), this.$recoveredBuilder) ? conversationState.getUnreadCount() + 1 : conversationState.getUnreadCount();
        }
        ExpandedNotification sbn = this.$entry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn, "entry.sbn");
        Notification notification = sbn.getNotification();
        Intrinsics.checkExpressionValueIsNotNull(notification, "entry.sbn.notification");
        return new ConversationNotificationManager.ConversationState(i, notification);
    }
}
