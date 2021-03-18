package com.android.systemui.statusbar.notification.collection.inflation;

import com.android.systemui.statusbar.FeatureFlags;
import com.android.systemui.statusbar.notification.collection.GroupEntry;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.NotifBindPipeline;
import com.android.systemui.statusbar.notification.row.RowContentBindParams;
import com.android.systemui.statusbar.notification.row.RowContentBindStage;
import com.android.systemui.statusbar.phone.NotificationGroupManager;

public class LowPriorityInflationHelper {
    private final FeatureFlags mFeatureFlags;
    private final NotificationGroupManager mGroupManager;
    private final RowContentBindStage mRowContentBindStage;

    LowPriorityInflationHelper(FeatureFlags featureFlags, NotificationGroupManager notificationGroupManager, RowContentBindStage rowContentBindStage) {
        this.mFeatureFlags = featureFlags;
        this.mGroupManager = notificationGroupManager;
        this.mRowContentBindStage = rowContentBindStage;
    }

    public void recheckLowPriorityViewAndInflate(NotificationEntry notificationEntry, ExpandableNotificationRow expandableNotificationRow) {
        RowContentBindParams rowContentBindParams = (RowContentBindParams) this.mRowContentBindStage.getStageParams(notificationEntry);
        boolean shouldUseLowPriorityView = shouldUseLowPriorityView(notificationEntry);
        if (!expandableNotificationRow.isRemoved() && expandableNotificationRow.isLowPriority() != shouldUseLowPriorityView) {
            rowContentBindParams.setUseLowPriority(shouldUseLowPriorityView);
            this.mRowContentBindStage.requestRebind(notificationEntry, new NotifBindPipeline.BindCallback(shouldUseLowPriorityView) {
                /* class com.android.systemui.statusbar.notification.collection.inflation.$$Lambda$LowPriorityInflationHelper$n1ql9BIAz5q3RsKneWWcajpBNlc */
                public final /* synthetic */ boolean f$1;

                {
                    this.f$1 = r2;
                }

                @Override // com.android.systemui.statusbar.notification.row.NotifBindPipeline.BindCallback
                public final void onBindFinished(NotificationEntry notificationEntry) {
                    ExpandableNotificationRow.this.setIsLowPriority(this.f$1);
                }
            });
        }
    }

    public boolean shouldUseLowPriorityView(NotificationEntry notificationEntry) {
        boolean z;
        if (this.mFeatureFlags.isNewNotifPipelineRenderingEnabled()) {
            z = notificationEntry.getParent() != GroupEntry.ROOT_ENTRY;
        } else {
            z = this.mGroupManager.isChildInGroupWithSummary(notificationEntry.getSbn());
        }
        return notificationEntry.isAmbient() && !z;
    }
}
