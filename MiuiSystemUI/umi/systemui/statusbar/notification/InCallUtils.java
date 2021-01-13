package com.android.systemui.statusbar.notification;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.policy.CallStateControllerImpl;
import com.miui.systemui.BuildConfig;
import com.miui.systemui.util.CommonUtil;
import java.util.Objects;

public class InCallUtils {
    public static boolean isInCallNotification(ExpandedNotification expandedNotification) {
        if (Objects.equals("call", expandedNotification.getNotification().category) && TextUtils.equals("incall", expandedNotification.getTag())) {
            return TextUtils.equals("com.android.incallui", expandedNotification.getOpPkg());
        }
        return false;
    }

    public static boolean isInCallNotificationHasVideoCall(ExpandedNotification expandedNotification) {
        return isInCallNotification(expandedNotification) && expandedNotification.getNotification().extras.getBoolean("hasVideoCall");
    }

    public static boolean isGlobalInCallNotification(Context context, String str, Notification notification) {
        if (BuildConfig.IS_INTERNATIONAL && Objects.equals("call", notification.category) && !TextUtils.equals(str, "com.android.incallui")) {
            return TextUtils.equals(str, ((TelecomManager) context.getSystemService("telecom")).getDefaultDialerPackage());
        }
        return false;
    }

    public static boolean isInCallNotificationHeadsUp(NotificationEntry notificationEntry) {
        if (notificationEntry == null || notificationEntry.getRow() == null || !notificationEntry.getRow().isHeadsUp() || !notificationEntry.getRow().isPinned() || !isInCallNotification(notificationEntry.getSbn())) {
            return false;
        }
        return true;
    }

    public static boolean isInCallScreenShowing() {
        return isCallScreenShowing() && getCallState() == 1;
    }

    public static boolean isCallScreenShowing() {
        String str;
        ComponentName topActivityLegacy = CommonUtil.getTopActivityLegacy();
        if (topActivityLegacy == null) {
            str = null;
        } else {
            str = topActivityLegacy.getClassName();
        }
        return "com.android.incallui.InCallActivity".equals(str);
    }

    private static int getCallState() {
        return ((CallStateControllerImpl) Dependency.get(CallStateControllerImpl.class)).getCallState();
    }
}
