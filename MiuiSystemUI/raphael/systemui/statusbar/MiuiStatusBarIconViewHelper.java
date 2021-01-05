package com.android.systemui.statusbar;

import android.content.Context;
import android.graphics.drawable.Icon;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.statusbar.notification.ExpandedNotification;

public class MiuiStatusBarIconViewHelper {
    public static void transformIconIfNeed(Context context, StatusBarIcon statusBarIcon, ExpandedNotification expandedNotification, String str, boolean z, boolean z2) {
        if (needChangeIcon(statusBarIcon, str, expandedNotification)) {
            if (z) {
                statusBarIcon.icon = Icon.createWithResource(context, Icons.getTintDrawableId(statusBarIcon.icon.getResId()));
            } else if (z2) {
                statusBarIcon.icon = Icon.createWithResource(context, Icons.getDarkDrawableId(statusBarIcon.icon.getResId()));
            } else {
                statusBarIcon.icon = Icon.createWithResource(context, Icons.getLightDrawableId(statusBarIcon.icon.getResId()));
            }
        }
    }

    public static boolean needChangeIcon(StatusBarIcon statusBarIcon, String str, ExpandedNotification expandedNotification) {
        return str != null && "com.android.systemui".equals(statusBarIcon.pkg) && expandedNotification == null;
    }

    public static boolean canUseTint(String str, StatusBarIcon statusBarIcon) {
        if (str == null || statusBarIcon == null) {
            return true;
        }
        return "com.android.systemui".equals(statusBarIcon.pkg);
    }

    public static int transformResId(int i, boolean z, boolean z2) {
        if (z) {
            return Icons.getTintDrawableId(i);
        }
        return z2 ? Icons.getLightDrawableId(i) : Icons.getDarkDrawableId(i);
    }
}
