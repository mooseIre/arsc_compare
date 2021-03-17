package com.android.systemui.statusbar.phone.dagger;

import com.android.systemui.statusbar.notification.row.RowContentBindStage;
import com.android.systemui.statusbar.phone.NotificationGroupAlertTransferHelper;

public interface StatusBarPhoneDependenciesModule {
    static NotificationGroupAlertTransferHelper provideNotificationGroupAlertTransferHelper(RowContentBindStage rowContentBindStage) {
        return new NotificationGroupAlertTransferHelper(rowContentBindStage);
    }
}
