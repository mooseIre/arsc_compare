package com.android.systemui.statusbar.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.graphics.drawable.Icon;
import android.os.Parcelable;

public class MiuiNotificationCompat {
    public static boolean isShowMiuiAction(Notification notification) {
        Notification.Action[] actionArr = notification.actions;
        if (actionArr == null || actionArr.length == 0 || !notification.extras.containsKey("miui.showAction")) {
            return false;
        }
        return notification.extras.getBoolean("miui.showAction", false);
    }

    public static CharSequence getMiuiActionTitle(Notification notification) {
        return isShowMiuiAction(notification) ? notification.actions[0].title : "";
    }

    public static boolean isEnableFloat(Notification notification) {
        if (notification.extras.containsKey("miui.enableFloat")) {
            return notification.extras.getBoolean("miui.enableFloat", true);
        }
        return notification.extraNotification.isEnableFloat();
    }

    public static boolean isEnableKeyguard(Notification notification) {
        if (notification.extras.containsKey("miui.enableKeyguard")) {
            return notification.extras.getBoolean("miui.enableKeyguard", true);
        }
        return notification.extraNotification.isEnableKeyguard();
    }

    public static int getFloatTime(Notification notification) {
        if (notification.extras.containsKey("miui.floatTime")) {
            return notification.extras.getInt("miui.floatTime", 5000);
        }
        return notification.extraNotification.getFloatTime();
    }

    public static void setFloatTime(Notification notification, int i) {
        notification.extras.putInt("miui.floatTime", i);
    }

    public static CharSequence getTargetPkg(Notification notification) {
        if (notification.extras.containsKey("miui.targetPkg")) {
            return notification.extras.getCharSequence("miui.targetPkg");
        }
        return notification.extraNotification.getTargetPkg();
    }

    public static int getMessageCount(Notification notification) {
        int i = notification.number;
        if (i > 0) {
            return i;
        }
        if (notification.extras.containsKey("miui.messageCount")) {
            return notification.extras.getInt("miui.messageCount", 1);
        }
        return notification.extraNotification.getMessageCount();
    }

    public static CharSequence getMessageClassName(Notification notification) {
        if (notification.extras.containsKey("miui.messageClassName")) {
            return notification.extras.getCharSequence("miui.messageClassName");
        }
        return notification.extraNotification.getMessageClassName();
    }

    public static PendingIntent getExitFloatingIntent(Notification notification) {
        if (notification.extras.containsKey("miui.exitFloating")) {
            Parcelable parcelable = notification.extras.getParcelable("miui.exitFloating");
            if (parcelable instanceof PendingIntent) {
                return (PendingIntent) parcelable;
            }
        }
        return notification.extraNotification.getExitFloatingIntent();
    }

    public static boolean isOnlyShowKeyguard(Notification notification) {
        return notification.extras.getBoolean("miui.onlyShowKeyguard", false);
    }

    public static boolean isKeptOnKeyguard(Notification notification) {
        return notification.extras.getBoolean("miui.keptOnKeyguard", false);
    }

    public static boolean isCustomHeight(Notification notification) {
        return notification.extras.getBoolean("miui.customHeight", false);
    }

    public static boolean isCustomHideBorder(Notification notification) {
        return notification.extras.getBoolean("miui.customHideBorder", false);
    }

    public static boolean isSystemWarnings(Notification notification) {
        return notification.extras.getBoolean("miui.systemWarnings", false);
    }

    public static boolean isShowingAtTail(Notification notification) {
        return notification.extras.getBoolean("miui.showAtTail", false);
    }

    public static boolean isPersistent(Notification notification) {
        return notification.extras.getBoolean("miui.isPersistent", false);
    }

    public static Icon getMiuiAppIcon(Notification notification) {
        Parcelable parcelable = notification.extras.getParcelable("miui.appIcon");
        if (parcelable instanceof Icon) {
            return (Icon) parcelable;
        }
        return null;
    }

    public static boolean isFloatWhenDnd(Notification notification) {
        return notification.extras.getBoolean("miui.floatWhenDnd", false);
    }

    public static boolean isGrayscaleIcon(Notification notification) {
        return notification.extras.getBoolean("miui.isGrayscaleIcon", false);
    }

    public static PendingIntent getLongPressIntent(Notification notification) {
        Parcelable parcelable = notification.extras.getParcelable("miui.longPressIntent");
        if (parcelable instanceof PendingIntent) {
            return (PendingIntent) parcelable;
        }
        return null;
    }
}
