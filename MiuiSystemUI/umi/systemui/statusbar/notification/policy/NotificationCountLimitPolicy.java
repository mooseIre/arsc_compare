package com.android.systemui.statusbar.notification.policy;

import android.util.Log;
import com.android.systemui.statusbar.notification.NotificationEntryListener;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class NotificationCountLimitPolicy {
    private NotificationEntryManager mEntryManager;

    public NotificationCountLimitPolicy(NotificationEntryManager notificationEntryManager) {
        this.mEntryManager = notificationEntryManager;
    }

    public void start() {
        this.mEntryManager.addNotificationEntryListener(new NotificationEntryListener() {
            /* class com.android.systemui.statusbar.notification.policy.NotificationCountLimitPolicy.AnonymousClass1 */

            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onNotificationAdded(NotificationEntry notificationEntry) {
                NotificationCountLimitPolicy.this.checkNotificationCountLimit(notificationEntry.getSbn().getPackageName());
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkNotificationCountLimit(String str) {
        List list = (List) this.mEntryManager.getVisibleNotifications().stream().filter(new Predicate(str) {
            /* class com.android.systemui.statusbar.notification.policy.$$Lambda$NotificationCountLimitPolicy$Nuj6QARJ5Tk90SSd1vymdMl4IrY */
            public final /* synthetic */ String f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return ((NotificationEntry) obj).getSbn().getPackageName().equals(this.f$0);
            }
        }).collect(Collectors.toList());
        if (list.size() > 10) {
            NotificationEntry notificationEntry = null;
            for (int i = 0; i < list.size(); i++) {
                notificationEntry = shouldRemove(notificationEntry, (NotificationEntry) list.get(i));
            }
            if (notificationEntry != null) {
                Log.d("NotificationCountLimitPolicy", "miui remove " + notificationEntry.getKey());
                this.mEntryManager.performRemoveNotification(notificationEntry.getSbn(), 0);
            }
        }
    }

    private NotificationEntry shouldRemove(NotificationEntry notificationEntry, NotificationEntry notificationEntry2) {
        if (notificationEntry == null) {
            return notificationEntry2;
        }
        if (notificationEntry2 == null) {
            return notificationEntry;
        }
        boolean isGroupSummary = notificationEntry.getSbn().getNotification().isGroupSummary();
        boolean isGroupSummary2 = notificationEntry2.getSbn().getNotification().isGroupSummary();
        if (isGroupSummary != isGroupSummary2) {
            if (isGroupSummary) {
                return notificationEntry2;
            }
            if (isGroupSummary2) {
                return notificationEntry;
            }
        }
        return notificationEntry.getSbn().getNotification().when < notificationEntry2.getSbn().getNotification().when ? notificationEntry : notificationEntry2;
    }
}
