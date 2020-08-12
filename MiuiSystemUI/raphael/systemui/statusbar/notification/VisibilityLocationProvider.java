package com.android.systemui.statusbar.notification;

import com.android.systemui.statusbar.ExpandableNotificationRow;

public interface VisibilityLocationProvider {
    boolean isInVisibleLocation(ExpandableNotificationRow expandableNotificationRow);
}
