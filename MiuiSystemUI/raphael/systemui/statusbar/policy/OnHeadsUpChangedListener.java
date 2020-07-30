package com.android.systemui.statusbar.policy;

import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.NotificationData;

public interface OnHeadsUpChangedListener {
    void onHeadsUpPinned(ExpandableNotificationRow expandableNotificationRow) {
    }

    void onHeadsUpPinnedModeChanged(boolean z) {
    }

    void onHeadsUpStateChanged(NotificationData.Entry entry, boolean z) {
    }

    void onHeadsUpUnPinned(ExpandableNotificationRow expandableNotificationRow) {
    }
}
