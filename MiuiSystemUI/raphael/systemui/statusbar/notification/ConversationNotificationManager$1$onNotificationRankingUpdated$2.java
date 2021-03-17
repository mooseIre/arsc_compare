package com.android.systemui.statusbar.notification;

import com.android.internal.widget.ConversationLayout;

/* compiled from: ConversationNotifications.kt */
final class ConversationNotificationManager$1$onNotificationRankingUpdated$2 implements Runnable {
    final /* synthetic */ boolean $important;
    final /* synthetic */ ConversationLayout $layout;

    ConversationNotificationManager$1$onNotificationRankingUpdated$2(ConversationLayout conversationLayout, boolean z) {
        this.$layout = conversationLayout;
        this.$important = z;
    }

    public final void run() {
        this.$layout.setIsImportantConversation(this.$important, true);
    }
}
