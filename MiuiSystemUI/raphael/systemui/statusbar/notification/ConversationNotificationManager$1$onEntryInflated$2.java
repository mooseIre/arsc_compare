package com.android.systemui.statusbar.notification;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;

/* access modifiers changed from: package-private */
/* compiled from: ConversationNotifications.kt */
public final class ConversationNotificationManager$1$onEntryInflated$2 implements ExpandableNotificationRow.OnExpansionChangedListener {
    final /* synthetic */ NotificationEntry $entry;
    final /* synthetic */ ConversationNotificationManager$1$onEntryInflated$1 $updateCount$1;

    ConversationNotificationManager$1$onEntryInflated$2(NotificationEntry notificationEntry, ConversationNotificationManager$1$onEntryInflated$1 conversationNotificationManager$1$onEntryInflated$1) {
        this.$entry = notificationEntry;
        this.$updateCount$1 = conversationNotificationManager$1$onEntryInflated$1;
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableNotificationRow.OnExpansionChangedListener
    public final void onExpansionChanged(final boolean z) {
        ExpandableNotificationRow row = this.$entry.getRow();
        if (row == null || !row.isShown() || !z) {
            this.$updateCount$1.invoke(z);
        } else {
            this.$entry.getRow().performOnIntrinsicHeightReached(new Runnable(this) {
                /* class com.android.systemui.statusbar.notification.ConversationNotificationManager$1$onEntryInflated$2.AnonymousClass1 */
                final /* synthetic */ ConversationNotificationManager$1$onEntryInflated$2 this$0;

                {
                    this.this$0 = r1;
                }

                public final void run() {
                    this.this$0.$updateCount$1.invoke(z);
                }
            });
        }
    }
}
