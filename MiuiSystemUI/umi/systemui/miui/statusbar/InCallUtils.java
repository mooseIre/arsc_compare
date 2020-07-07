package com.android.systemui.miui.statusbar;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.Util;
import com.android.systemui.statusbar.CallStateController;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import java.util.Objects;

public class InCallUtils {
    public static boolean isInCallNotification(ExpandedNotification expandedNotification) {
        if (Objects.equals("call", expandedNotification.getNotification().category) && TextUtils.equals("incall", expandedNotification.getTag())) {
            return TextUtils.equals("com.android.incallui", expandedNotification.getBasePkg());
        }
        return false;
    }

    public static boolean isInCallNotificationHasVideoCall(ExpandedNotification expandedNotification) {
        return isInCallNotification(expandedNotification) && expandedNotification.getNotification().extras.getBoolean("hasVideoCall");
    }

    public static boolean isGlobalInCallNotification(Context context, ExpandedNotification expandedNotification) {
        if (Constants.IS_INTERNATIONAL && Objects.equals("call", expandedNotification.getNotification().category) && !TextUtils.equals(expandedNotification.getBasePkg(), "com.android.incallui")) {
            return TextUtils.equals(expandedNotification.getBasePkg(), ((TelecomManager) context.getSystemService("telecom")).getDefaultDialerPackage());
        }
        return false;
    }

    public static void goInCallScreen(Context context) {
        goInCallScreen(context, (Bundle) null);
    }

    public static void goInCallScreen(Context context, Bundle bundle) {
        Intent intent = new Intent("android.intent.action.MAIN");
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        intent.setFlags(277086208);
        if (getCallState() != 0) {
            intent.setClassName("com.android.incallui", "com.android.incallui.InCallActivity");
        }
        try {
            context.startActivityAsUser(intent, UserHandle.CURRENT);
        } catch (ActivityNotFoundException unused) {
        }
    }

    public static boolean isInCallNotificationHeadsUp(HeadsUpManager.HeadsUpEntry headsUpEntry) {
        return headsUpEntry != null && isInCallNotificationHeadsUp(headsUpEntry.entry);
    }

    public static boolean isInCallNotificationHeadsUp(NotificationData.Entry entry) {
        return entry.row.isHeadsUp() && entry.row.isPinned() && isInCallNotification(entry.notification);
    }

    public static boolean isInCallScreenShowing(Context context) {
        return isCallScreenShowing(context) && getCallState() == 1;
    }

    public static boolean isCallScreenShowing(Context context) {
        String str;
        ComponentName topActivity = Util.getTopActivity(context);
        if (topActivity == null) {
            str = null;
        } else {
            str = topActivity.getClassName();
        }
        return "com.android.incallui.InCallActivity".equals(str);
    }

    private static int getCallState() {
        return ((CallStateController) Dependency.get(CallStateController.class)).getCallState();
    }
}
