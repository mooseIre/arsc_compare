package com.android.systemui.statusbar.notification.row.wrapper;

import android.content.Context;
import android.view.NotificationHeaderView;
import android.view.View;
import com.android.systemui.C0012R$id;
import com.android.systemui.statusbar.notification.NotificationSettingsHelper;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.MiuiExpandableNotificationRow;

public class NotificationViewWrapperInjector {
    public static NotificationViewWrapper wrap(Context context, View view, ExpandableNotificationRow expandableNotificationRow) {
        if (NotificationSettingsHelper.showMiuiStyle()) {
            if (view.getId() != C0012R$id.status_bar_latest_event_content) {
                return new MiuiNotificationCustomViewWrapper(context, view, expandableNotificationRow);
            }
            if ("oneLine".equals(view.getTag())) {
                return new MiuiNotificationOneLineViewWrapper(context, view, expandableNotificationRow);
            }
            if ("base".equals(view.getTag()) || "big".equals(view.getTag())) {
                return new MiuiNotificationTemplateViewWrapper(context, view, expandableNotificationRow);
            }
            if ("bigPicture".equals(view.getTag())) {
                return new MiuiNotificationBigPictureViewWrapper(context, view, expandableNotificationRow);
            }
            if ("bigText".equals(view.getTag())) {
                return new MiuiNotificationBigTextViewWrapper(context, view, expandableNotificationRow);
            }
            if ("inbox".equals(view.getTag())) {
                return new MiuiNotificationInboxViewWrapper(context, view, expandableNotificationRow);
            }
            return null;
        } else if (view.getId() != 16909500 && !(view instanceof NotificationHeaderView)) {
            return new MiuiNotificationCustomViewWrapper(context, view, expandableNotificationRow);
        } else {
            return null;
        }
    }

    public static int getExtraMeasureHeight(NotificationViewWrapper notificationViewWrapper, ExpandableNotificationRow expandableNotificationRow) {
        if ((notificationViewWrapper instanceof MiuiNotificationCustomViewWrapper) || (notificationViewWrapper instanceof NotificationCustomViewWrapper) || (notificationViewWrapper instanceof NotificationDecoratedCustomViewWrapper) || !(expandableNotificationRow instanceof MiuiExpandableNotificationRow)) {
            return 0;
        }
        return 0 + ((MiuiExpandableNotificationRow) expandableNotificationRow).getMiniBarHeight();
    }
}
