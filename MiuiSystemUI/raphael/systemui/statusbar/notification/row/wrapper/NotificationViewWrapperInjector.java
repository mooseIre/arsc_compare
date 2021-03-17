package com.android.systemui.statusbar.notification.row.wrapper;

import android.app.Notification;
import android.content.Context;
import android.view.NotificationHeaderView;
import android.view.View;
import com.android.internal.widget.ConversationLayout;
import com.android.systemui.C0015R$id;
import com.android.systemui.statusbar.notification.NotificationSettingsHelper;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.MiuiExpandableNotificationRow;

public class NotificationViewWrapperInjector {
    public static NotificationViewWrapper wrap(Context context, View view, ExpandableNotificationRow expandableNotificationRow) {
        if (view.getId() == C0015R$id.status_bar_latest_event_content && "oneLine".equals(view.getTag())) {
            return new MiuiNotificationOneLineViewWrapper(context, view, expandableNotificationRow);
        }
        if (NotificationSettingsHelper.showMiuiStyle()) {
            if (view.getId() != C0015R$id.status_bar_latest_event_content) {
                if (view.getId() == 16909496) {
                    if ("conversation".equals(view.getTag())) {
                        return new MiuiNotificationConversationTemplateViewWrapper(context, (ConversationLayout) view, expandableNotificationRow);
                    }
                    if ("media".equals(view.getTag()) || "bigMediaNarrow".equals(view.getTag())) {
                        return null;
                    }
                }
                return getMiuiCustomViewWrapper(context, view, expandableNotificationRow);
            } else if ("oneLine".equals(view.getTag())) {
                return new MiuiNotificationOneLineViewWrapper(context, view, expandableNotificationRow);
            } else {
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
            }
        } else if (view.getId() != 16909496 && !(view instanceof NotificationHeaderView)) {
            return getMiuiCustomViewWrapper(context, view, expandableNotificationRow);
        } else {
            return null;
        }
    }

    private static NotificationViewWrapper getMiuiCustomViewWrapper(Context context, View view, ExpandableNotificationRow expandableNotificationRow) {
        if (Notification.DecoratedCustomViewStyle.class.equals(expandableNotificationRow.getEntry().getSbn().getNotification().getNotificationStyle())) {
            return new MiuiNotificationDecoratedCustomViewWrapper(context, view, expandableNotificationRow);
        }
        return new MiuiNotificationCustomViewWrapper(context, view, expandableNotificationRow);
    }

    public static int getExtraHeight(NotificationViewWrapper notificationViewWrapper, ExpandableNotificationRow expandableNotificationRow) {
        return MiuiNotificationCustomViewWrapper.getExtraMeasureHeight(notificationViewWrapper) + 0 + getExtraMeasureHeight(notificationViewWrapper, expandableNotificationRow);
    }

    public static int getExtraMeasureHeight(NotificationViewWrapper notificationViewWrapper, ExpandableNotificationRow expandableNotificationRow) {
        if ((notificationViewWrapper instanceof MiuiNotificationCustomViewWrapper) || (notificationViewWrapper instanceof NotificationCustomViewWrapper) || (notificationViewWrapper instanceof NotificationDecoratedCustomViewWrapper) || !(expandableNotificationRow instanceof MiuiExpandableNotificationRow)) {
            return 0;
        }
        return 0 + ((MiuiExpandableNotificationRow) expandableNotificationRow).getMiniBarHeight();
    }
}
