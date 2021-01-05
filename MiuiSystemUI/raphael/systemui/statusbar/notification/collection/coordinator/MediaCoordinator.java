package com.android.systemui.statusbar.notification.collection.coordinator;

import com.android.systemui.media.MediaDataManagerKt;
import com.android.systemui.media.MediaFeatureFlag;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifFilter;

public class MediaCoordinator implements Coordinator {
    /* access modifiers changed from: private */
    public final Boolean mIsMediaFeatureEnabled;
    private final NotifFilter mMediaFilter = new NotifFilter("MediaCoordinator") {
        public boolean shouldFilterOut(NotificationEntry notificationEntry, long j) {
            return MediaCoordinator.this.mIsMediaFeatureEnabled.booleanValue() && MediaDataManagerKt.isMediaNotification(notificationEntry.getSbn());
        }
    };

    public MediaCoordinator(MediaFeatureFlag mediaFeatureFlag) {
        mediaFeatureFlag.getEnabled();
        this.mIsMediaFeatureEnabled = Boolean.TRUE;
    }

    public void attach(NotifPipeline notifPipeline) {
        notifPipeline.addFinalizeFilter(this.mMediaFilter);
    }
}
