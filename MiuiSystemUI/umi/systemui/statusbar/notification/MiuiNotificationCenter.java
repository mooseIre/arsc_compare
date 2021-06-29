package com.android.systemui.statusbar.notification;

import android.content.ComponentName;
import android.content.Context;
import miui.util.Log;

public class MiuiNotificationCenter {
    private static String TAG = "NcSystem";

    public static void start(Context context) {
        disableComponent(context, "com.android.settings", "com.android.settings.Settings$NotificationAppListActivity");
    }

    private static void disableComponent(Context context, String str, String str2) {
        String str3 = TAG;
        Log.d(str3, "disableComponent " + str2);
        context.getPackageManager().setComponentEnabledSetting(new ComponentName(str, str2), 2, 1);
    }
}
