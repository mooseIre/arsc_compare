package com.android.systemui.statusbar.policy;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;

public interface OnHeadsUpChangedListener {
    void onHeadsUpPinned(NotificationEntry notificationEntry) {
    }

    void onHeadsUpPinnedModeChanged(boolean z) {
    }

    void onHeadsUpStateChanged(NotificationEntry notificationEntry, boolean z) {
    }

    void onHeadsUpUnPinned(NotificationEntry notificationEntry) {
    }
}
