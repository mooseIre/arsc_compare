package com.android.systemui.statusbar.notification.collection.coordinator;

import com.android.systemui.media.MediaDataManagerKt;
import com.android.systemui.media.MediaFeatureFlag;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifFilter;

public class MediaCoordinator implements Coordinator {
    private final Boolean mIsMediaFeatureEnabled;
    private final NotifFilter mMediaFilter = new NotifFilter("MediaCoordinator") {
        /* class com.android.systemui.statusbar.notification.collection.coordinator.MediaCoordinator.AnonymousClass1 */

        @Override // com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifFilter
        public boolean shouldFilterOut(NotificationEntry notificationEntry, long j) {
            return MediaCoordinator.this.mIsMediaFeatureEnabled.booleanValue() && MediaDataManagerKt.isMediaNotification(notificationEntry.getSbn());
        }
    };

    public MediaCoordinator(MediaFeatureFlag mediaFeatureFlag) {
        mediaFeatureFlag.getEnabled();
        this.mIsMediaFeatureEnabled = Boolean.TRUE;
    }

    @Override // com.android.systemui.statusbar.notification.collection.coordinator.Coordinator
    public void attach(NotifPipeline notifPipeline) {
        notifPipeline.addFinalizeFilter(this.mMediaFilter);
    }
}
