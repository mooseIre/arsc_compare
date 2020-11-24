package com.android.systemui.statusbar.notification.row;

import com.android.internal.logging.UiEventLogger;

enum NotificationAppOpsEvent implements UiEventLogger.UiEventEnum {
    NOTIFICATION_APP_OPS_OPEN(597),
    NOTIFICATION_APP_OPS_CLOSE(598),
    NOTIFICATION_APP_OPS_SETTINGS_CLICK(599);
    
    private final int mId;

    private NotificationAppOpsEvent(int i) {
        this.mId = i;
    }

    public int getId() {
        return this.mId;
    }
}
