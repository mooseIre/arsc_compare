package com.android.systemui.statusbar.notification.collection.coordinator;

import com.android.internal.statusbar.NotificationVisibility;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.statusbar.notification.collection.NotifCollection;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifFilter;
import com.android.systemui.statusbar.notification.collection.notifcollection.DismissedByUserStats;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifDismissInterceptor;
import com.android.systemui.statusbar.notification.logging.NotificationLogger;
import java.util.HashSet;
import java.util.Set;

public class BubbleCoordinator implements Coordinator {
    private final BubbleController mBubbleController;
    private final NotifDismissInterceptor mDismissInterceptor = new NotifDismissInterceptor() {
        /* class com.android.systemui.statusbar.notification.collection.coordinator.BubbleCoordinator.AnonymousClass2 */

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifDismissInterceptor
        public String getName() {
            return "BubbleCoordinator";
        }

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifDismissInterceptor
        public void setCallback(NotifDismissInterceptor.OnEndDismissInterception onEndDismissInterception) {
            BubbleCoordinator.this.mOnEndDismissInterception = onEndDismissInterception;
        }

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifDismissInterceptor
        public boolean shouldInterceptDismissal(NotificationEntry notificationEntry) {
            if (BubbleCoordinator.this.mBubbleController.handleDismissalInterception(notificationEntry)) {
                BubbleCoordinator.this.mInterceptedDismissalEntries.add(notificationEntry.getKey());
                return true;
            }
            BubbleCoordinator.this.mInterceptedDismissalEntries.remove(notificationEntry.getKey());
            return false;
        }

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifDismissInterceptor
        public void cancelDismissInterception(NotificationEntry notificationEntry) {
            BubbleCoordinator.this.mInterceptedDismissalEntries.remove(notificationEntry.getKey());
        }
    };
    private final Set<String> mInterceptedDismissalEntries = new HashSet();
    private final BubbleController.NotifCallback mNotifCallback = new BubbleController.NotifCallback() {
        /* class com.android.systemui.statusbar.notification.collection.coordinator.BubbleCoordinator.AnonymousClass3 */

        @Override // com.android.systemui.bubbles.BubbleController.NotifCallback
        public void maybeCancelSummary(NotificationEntry notificationEntry) {
        }

        @Override // com.android.systemui.bubbles.BubbleController.NotifCallback
        public void removeNotification(NotificationEntry notificationEntry, int i) {
            if (BubbleCoordinator.this.isInterceptingDismissal(notificationEntry)) {
                BubbleCoordinator.this.mInterceptedDismissalEntries.remove(notificationEntry.getKey());
                BubbleCoordinator.this.mOnEndDismissInterception.onEndDismissInterception(BubbleCoordinator.this.mDismissInterceptor, notificationEntry, BubbleCoordinator.this.createDismissedByUserStats(notificationEntry));
            } else if (BubbleCoordinator.this.mNotifPipeline.getAllNotifs().contains(notificationEntry)) {
                BubbleCoordinator.this.mNotifCollection.dismissNotification(notificationEntry, BubbleCoordinator.this.createDismissedByUserStats(notificationEntry));
            }
        }

        @Override // com.android.systemui.bubbles.BubbleController.NotifCallback
        public void invalidateNotifications(String str) {
            BubbleCoordinator.this.mNotifFilter.invalidateList();
        }
    };
    private final NotifCollection mNotifCollection;
    private final NotifFilter mNotifFilter = new NotifFilter("BubbleCoordinator") {
        /* class com.android.systemui.statusbar.notification.collection.coordinator.BubbleCoordinator.AnonymousClass1 */

        @Override // com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifFilter
        public boolean shouldFilterOut(NotificationEntry notificationEntry, long j) {
            return BubbleCoordinator.this.mBubbleController.isBubbleNotificationSuppressedFromShade(notificationEntry);
        }
    };
    private NotifPipeline mNotifPipeline;
    private NotifDismissInterceptor.OnEndDismissInterception mOnEndDismissInterception;

    public BubbleCoordinator(BubbleController bubbleController, NotifCollection notifCollection) {
        this.mBubbleController = bubbleController;
        this.mNotifCollection = notifCollection;
    }

    @Override // com.android.systemui.statusbar.notification.collection.coordinator.Coordinator
    public void attach(NotifPipeline notifPipeline) {
        this.mNotifPipeline = notifPipeline;
        notifPipeline.addNotificationDismissInterceptor(this.mDismissInterceptor);
        this.mNotifPipeline.addFinalizeFilter(this.mNotifFilter);
        this.mBubbleController.addNotifCallback(this.mNotifCallback);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isInterceptingDismissal(NotificationEntry notificationEntry) {
        return this.mInterceptedDismissalEntries.contains(notificationEntry.getKey());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private DismissedByUserStats createDismissedByUserStats(NotificationEntry notificationEntry) {
        return new DismissedByUserStats(0, 1, NotificationVisibility.obtain(notificationEntry.getKey(), notificationEntry.getRanking().getRank(), this.mNotifPipeline.getShadeListCount(), true, NotificationLogger.getNotificationLocation(notificationEntry)));
    }
}
