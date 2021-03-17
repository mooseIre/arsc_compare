package com.android.systemui.statusbar.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.service.notification.NotificationListenerService;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ConversationNotifications.kt */
public final class ConversationNotificationProcessor {
    private final ConversationNotificationManager conversationNotificationManager;
    private final LauncherApps launcherApps;

    public ConversationNotificationProcessor(@NotNull LauncherApps launcherApps2, @NotNull ConversationNotificationManager conversationNotificationManager2) {
        Intrinsics.checkParameterIsNotNull(launcherApps2, "launcherApps");
        Intrinsics.checkParameterIsNotNull(conversationNotificationManager2, "conversationNotificationManager");
        this.launcherApps = launcherApps2;
        this.conversationNotificationManager = conversationNotificationManager2;
    }

    public final void processNotification(@NotNull NotificationEntry notificationEntry, @NotNull Notification.Builder builder) {
        Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
        Intrinsics.checkParameterIsNotNull(builder, "recoveredBuilder");
        Notification.Style style = builder.getStyle();
        if (!(style instanceof Notification.MessagingStyle)) {
            style = null;
        }
        Notification.MessagingStyle messagingStyle = (Notification.MessagingStyle) style;
        if (messagingStyle != null) {
            NotificationListenerService.Ranking ranking = notificationEntry.getRanking();
            Intrinsics.checkExpressionValueIsNotNull(ranking, "entry.ranking");
            NotificationChannel channel = ranking.getChannel();
            Intrinsics.checkExpressionValueIsNotNull(channel, "entry.ranking.channel");
            messagingStyle.setConversationType(channel.isImportantConversation() ? 2 : 1);
            NotificationListenerService.Ranking ranking2 = notificationEntry.getRanking();
            Intrinsics.checkExpressionValueIsNotNull(ranking2, "entry.ranking");
            ShortcutInfo shortcutInfo = ranking2.getShortcutInfo();
            if (shortcutInfo != null) {
                messagingStyle.setShortcutIcon(this.launcherApps.getShortcutIcon(shortcutInfo));
                CharSequence label = shortcutInfo.getLabel();
                if (label != null) {
                    messagingStyle.setConversationTitle(label);
                }
            }
            messagingStyle.setUnreadMessageCount(this.conversationNotificationManager.getUnreadCount(notificationEntry, builder));
        }
    }
}
