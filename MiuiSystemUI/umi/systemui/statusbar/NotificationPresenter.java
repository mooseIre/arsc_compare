package com.android.systemui.statusbar;

import com.android.systemui.statusbar.notification.row.ActivatableNotificationView;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;

public interface NotificationPresenter extends ExpandableNotificationRow.OnExpandClickListener, ActivatableNotificationView.OnActivatedListener {
    int getMaxNotificationsWhileLocked(boolean z);

    boolean isCollapsing();

    boolean isDeviceInVrMode();

    boolean isPresenterFullyCollapsed();

    void onUpdateRowStates();

    void onUserSwitched(int i);

    void updateMediaMetaData(boolean z, boolean z2);

    void updateNotificationViews(String str);
}
