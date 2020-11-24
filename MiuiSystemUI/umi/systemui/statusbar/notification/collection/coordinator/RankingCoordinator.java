package com.android.systemui.statusbar.notification.collection.coordinator;

import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifFilter;

public class RankingCoordinator implements Coordinator {
    /* access modifiers changed from: private */
    public final NotifFilter mDozingFilter = new NotifFilter("IsDozingFilter") {
        public boolean shouldFilterOut(NotificationEntry notificationEntry, long j) {
            if (RankingCoordinator.this.mStatusBarStateController.isDozing() && notificationEntry.shouldSuppressAmbient()) {
                return true;
            }
            if (RankingCoordinator.this.mStatusBarStateController.isDozing() || !notificationEntry.shouldSuppressNotificationList()) {
                return false;
            }
            return true;
        }
    };
    private final StatusBarStateController.StateListener mStatusBarStateCallback = new StatusBarStateController.StateListener() {
        public void onDozingChanged(boolean z) {
            RankingCoordinator.this.mDozingFilter.invalidateList();
        }
    };
    /* access modifiers changed from: private */
    public final StatusBarStateController mStatusBarStateController;
    private final NotifFilter mSuspendedFilter = new NotifFilter(this, "IsSuspendedFilter") {
        public boolean shouldFilterOut(NotificationEntry notificationEntry, long j) {
            return notificationEntry.getRanking().isSuspended();
        }
    };

    public RankingCoordinator(StatusBarStateController statusBarStateController) {
        this.mStatusBarStateController = statusBarStateController;
    }

    public void attach(NotifPipeline notifPipeline) {
        this.mStatusBarStateController.addCallback(this.mStatusBarStateCallback);
        notifPipeline.addPreGroupFilter(this.mSuspendedFilter);
        notifPipeline.addPreGroupFilter(this.mDozingFilter);
    }
}
