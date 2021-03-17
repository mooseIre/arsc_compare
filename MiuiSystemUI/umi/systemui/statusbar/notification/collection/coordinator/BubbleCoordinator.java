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
    /* access modifiers changed from: private */
    public final BubbleController mBubbleController;
    /* access modifiers changed from: private */
    public final NotifDismissInterceptor mDismissInterceptor = new NotifDismissInterceptor() {
        public String getName() {
            return "BubbleCoordinator";
        }

        public void setCallback(NotifDismissInterceptor.OnEndDismissInterception onEndDismissInterception) {
            NotifDismissInterceptor.OnEndDismissInterception unused = BubbleCoordinator.this.mOnEndDismissInterception = onEndDismissInterception;
        }

        public boolean shouldInterceptDismissal(NotificationEntry notificationEntry) {
            if (BubbleCoordinator.this.mBubbleController.handleDismissalInterception(notificationEntry)) {
                BubbleCoordinator.this.mInterceptedDismissalEntries.add(notificationEntry.getKey());
                return true;
            }
            BubbleCoordinator.this.mInterceptedDismissalEntries.remove(notificationEntry.getKey());
            return false;
        }

        public void cancelDismissInterception(NotificationEntry notificationEntry) {
            BubbleCoordinator.this.mInterceptedDismissalEntries.remove(notificationEntry.getKey());
        }
    };
    /* access modifiers changed from: private */
    public final Set<String> mInterceptedDismissalEntries = new HashSet();
    private final BubbleController.NotifCallback mNotifCallback = new BubbleController.NotifCallback() {
        public void maybeCancelSummary(NotificationEntry notificationEntry) {
        }

        public void removeNotification(NotificationEntry notificationEntry, int i) {
            if (BubbleCoordinator.this.isInterceptingDismissal(notificationEntry)) {
                BubbleCoordinator.this.mInterceptedDismissalEntries.remove(notificationEntry.getKey());
                BubbleCoordinator.this.mOnEndDismissInterception.onEndDismissInterception(BubbleCoordinator.this.mDismissInterceptor, notificationEntry, BubbleCoordinator.this.createDismissedByUserStats(notificationEntry));
            } else if (BubbleCoordinator.this.mNotifPipeline.getAllNotifs().contains(notificationEntry)) {
                BubbleCoordinator.this.mNotifCollection.dismissNotification(notificationEntry, BubbleCoordinator.this.createDismissedByUserStats(notificationEntry));
            }
        }

        public void invalidateNotifications(String str) {
            BubbleCoordinator.this.mNotifFilter.invalidateList();
        }
    };
    /* access modifiers changed from: private */
    public final NotifCollection mNotifCollection;
    /* access modifiers changed from: private */
    public final NotifFilter mNotifFilter = new NotifFilter("BubbleCoordinator") {
        public boolean shouldFilterOut(NotificationEntry notificationEntry, long j) {
            return BubbleCoordinator.this.mBubbleController.isBubbleNotificationSuppressedFromShade(notificationEntry);
        }
    };
    /* access modifiers changed from: private */
    public NotifPipeline mNotifPipeline;
    /* access modifiers changed from: private */
    public NotifDismissInterceptor.OnEndDismissInterception mOnEndDismissInterception;

    public BubbleCoordinator(BubbleController bubbleController, NotifCollection notifCollection) {
        this.mBubbleController = bubbleController;
        this.mNotifCollection = notifCollection;
    }

    public void attach(NotifPipeline notifPipeline) {
        this.mNotifPipeline = notifPipeline;
        notifPipeline.addNotificationDismissInterceptor(this.mDismissInterceptor);
        this.mNotifPipeline.addFinalizeFilter(this.mNotifFilter);
        this.mBubbleController.addNotifCallback(this.mNotifCallback);
    }

    /* access modifiers changed from: private */
    public boolean isInterceptingDismissal(NotificationEntry notificationEntry) {
        return this.mInterceptedDismissalEntries.contains(notificationEntry.getKey());
    }

    /* access modifiers changed from: private */
    public DismissedByUserStats createDismissedByUserStats(NotificationEntry notificationEntry) {
        return new DismissedByUserStats(0, 1, NotificationVisibility.obtain(notificationEntry.getKey(), notificationEntry.getRanking().getRank(), this.mNotifPipeline.getShadeListCount(), true, NotificationLogger.getNotificationLocation(notificationEntry)));
    }
}
