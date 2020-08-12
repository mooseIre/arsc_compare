package com.android.systemui.statusbar.notification;

import android.content.Context;
import android.view.View;
import com.android.systemui.statusbar.ExpandableNotificationRow;

public class NotificationInCallViewWrapper extends NotificationViewWrapper {
    protected NotificationInCallViewWrapper(Context context, View view, ExpandableNotificationRow expandableNotificationRow) {
        super(context, view, expandableNotificationRow);
    }
}
