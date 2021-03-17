package com.android.systemui.statusbar.notification.stack;

import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.plugins.statusbar.NotificationSwipeActionHelper;
import com.android.systemui.statusbar.notification.ActivityLaunchAnimator;
import com.android.systemui.statusbar.notification.NotificationActivityStarter;
import com.android.systemui.statusbar.notification.VisibilityLocationProvider;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.SimpleNotificationListContainer;
import com.android.systemui.statusbar.notification.logging.NotificationLogger;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.ExpandableView;

public interface NotificationListContainer extends ExpandableView.OnHeightChangedListener, VisibilityLocationProvider, SimpleNotificationListContainer {
    void addContainerView(View view);

    default void applyExpandAnimationParams(ActivityLaunchAnimator.ExpandAnimationParameters expandAnimationParameters) {
    }

    default void bindRow(ExpandableNotificationRow expandableNotificationRow) {
    }

    void changeViewPosition(ExpandableView expandableView, int i);

    void cleanUpViewStateForEntry(NotificationEntry notificationEntry);

    default boolean containsView(View view) {
        return true;
    }

    void generateAddAnimation(ExpandableView expandableView, boolean z);

    @Override // com.android.systemui.statusbar.notification.collection.SimpleNotificationListContainer
    void generateChildOrderChangedEvent();

    @Override // com.android.systemui.statusbar.notification.collection.SimpleNotificationListContainer
    View getContainerChildAt(int i);

    @Override // com.android.systemui.statusbar.notification.collection.SimpleNotificationListContainer
    int getContainerChildCount();

    NotificationSwipeActionHelper getSwipeActionHelper();

    ViewGroup getViewParentForNotification(NotificationEntry notificationEntry);

    boolean hasPulsingNotifications();

    void notifyGroupChildAdded(ExpandableView expandableView);

    void notifyGroupChildRemoved(ExpandableView expandableView, ViewGroup viewGroup);

    default void onNotificationViewUpdateFinished() {
    }

    void removeContainerView(View view);

    void resetExposedMenuView(boolean z, boolean z2);

    void setChildLocationsChangedListener(NotificationLogger.OnChildLocationsChangedListener onChildLocationsChangedListener);

    @Override // com.android.systemui.statusbar.notification.collection.SimpleNotificationListContainer
    void setChildTransferInProgress(boolean z);

    default void setExpandingNotification(ExpandableNotificationRow expandableNotificationRow) {
    }

    void setMaxDisplayedNotifications(int i);

    void setNotificationActivityStarter(NotificationActivityStarter notificationActivityStarter);

    default void setWillExpand(boolean z) {
    }
}
