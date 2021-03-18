package com.android.systemui.statusbar.notification.collection.coordinator;

import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifFilter;

public class HideLocallyDismissedNotifsCoordinator implements Coordinator {
    private final NotifFilter mFilter = new NotifFilter(this, "HideLocallyDismissedNotifsFilter") {
        /* class com.android.systemui.statusbar.notification.collection.coordinator.HideLocallyDismissedNotifsCoordinator.AnonymousClass1 */

        @Override // com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifFilter
        public boolean shouldFilterOut(NotificationEntry notificationEntry, long j) {
            return notificationEntry.getDismissState() != NotificationEntry.DismissState.NOT_DISMISSED;
        }
    };

    @Override // com.android.systemui.statusbar.notification.collection.coordinator.Coordinator
    public void attach(NotifPipeline notifPipeline) {
        notifPipeline.addPreGroupFilter(this.mFilter);
    }
}
