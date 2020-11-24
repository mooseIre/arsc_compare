package com.android.systemui.statusbar.notification.stack;

import android.view.View;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import java.util.List;

public interface NotificationListItem {
    void addChildNotification(NotificationListItem notificationListItem, int i);

    boolean applyChildOrder(List<? extends NotificationListItem> list, VisualStabilityManager visualStabilityManager, VisualStabilityManager.Callback callback);

    List<? extends NotificationListItem> getAttachedChildren();

    NotificationEntry getEntry();

    View getView();

    boolean isBlockingHelperShowing();

    boolean isSummaryWithChildren();

    void removeAllChildren();

    void removeChildNotification(NotificationListItem notificationListItem);

    void setUntruncatedChildCount(int i);
}
