package com.android.systemui.statusbar.notification.row.wrapper;

import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.android.systemui.statusbar.notification.NotificationUtil;

public class NotificationHeaderViewWrapperInjector {
    public static void setAppIcon(ImageView imageView, ExpandedNotification expandedNotification) {
        if (NotificationUtil.shouldSubstituteSmallIcon(expandedNotification)) {
            imageView.setImageDrawable(expandedNotification.getAppIcon());
        }
    }

    public static void setAppNameText(TextView textView, ExpandedNotification expandedNotification) {
        if (!TextUtils.equals(textView.getText(), expandedNotification.getAppName())) {
            textView.setText(expandedNotification.getAppName());
        }
    }
}
