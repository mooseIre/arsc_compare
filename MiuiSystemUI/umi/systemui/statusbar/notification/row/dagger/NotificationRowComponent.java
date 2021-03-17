package com.android.systemui.statusbar.notification.row.dagger;

import com.android.systemui.statusbar.notification.row.ActivatableNotificationView;
import com.android.systemui.statusbar.notification.row.ActivatableNotificationViewController;

public interface NotificationRowComponent {

    public interface Builder {
        Builder activatableNotificationView(ActivatableNotificationView activatableNotificationView);

        NotificationRowComponent build();
    }

    ActivatableNotificationViewController getActivatableNotificationViewController();
}
