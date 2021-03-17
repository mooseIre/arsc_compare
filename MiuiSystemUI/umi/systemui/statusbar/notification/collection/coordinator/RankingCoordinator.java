package com.android.systemui.statusbar.notification.collection.coordinator;

import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifFilter;

public class RankingCoordinator implements Coordinator {
    private final NotifFilter mDozingFilter = new NotifFilter("IsDozingFilter") {
        /* class com.android.systemui.statusbar.notification.collection.coordinator.RankingCoordinator.AnonymousClass2 */

        @Override // com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifFilter
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
        /* class com.android.systemui.statusbar.notification.collection.coordinator.RankingCoordinator.AnonymousClass3 */

        @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
        public void onDozingChanged(boolean z) {
            RankingCoordinator.this.mDozingFilter.invalidateList();
        }
    };
    private final StatusBarStateController mStatusBarStateController;
    private final NotifFilter mSuspendedFilter = new NotifFilter(this, "IsSuspendedFilter") {
        /* class com.android.systemui.statusbar.notification.collection.coordinator.RankingCoordinator.AnonymousClass1 */

        @Override // com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifFilter
        public boolean shouldFilterOut(NotificationEntry notificationEntry, long j) {
            return notificationEntry.getRanking().isSuspended();
        }
    };

    public RankingCoordinator(StatusBarStateController statusBarStateController) {
        this.mStatusBarStateController = statusBarStateController;
    }

    @Override // com.android.systemui.statusbar.notification.collection.coordinator.Coordinator
    public void attach(NotifPipeline notifPipeline) {
        this.mStatusBarStateController.addCallback(this.mStatusBarStateCallback);
        notifPipeline.addPreGroupFilter(this.mSuspendedFilter);
        notifPipeline.addPreGroupFilter(this.mDozingFilter);
    }
}
