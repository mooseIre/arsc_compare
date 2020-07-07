package com.android.systemui.statusbar;

import com.android.systemui.statusbar.ActivatableNotificationView;
import com.android.systemui.statusbar.ExpandableNotificationRow;

public interface NotificationPresenter extends ExpandableNotificationRow.OnExpandClickListener, ActivatableNotificationView.OnActivatedListener {
    boolean isCollapsing();

    boolean isPresenterFullyCollapsed();
}
