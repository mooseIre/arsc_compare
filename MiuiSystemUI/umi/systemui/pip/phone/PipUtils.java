package com.android.systemui.pip.phone;

import android.app.ActivityManager;
import android.app.ActivityManagerCompat;
import android.app.IActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

public class PipUtils {
    public static ComponentName getTopPinnedActivity(Context context, IActivityManager iActivityManager) {
        try {
            String packageName = context.getPackageName();
            ActivityManager.StackInfo stackInfo = ActivityManagerCompat.getStackInfo(4, 2, 0);
            if (stackInfo == null || stackInfo.taskIds == null || stackInfo.taskIds.length <= 0) {
                return null;
            }
            for (int length = stackInfo.taskNames.length - 1; length >= 0; length--) {
                ComponentName unflattenFromString = ComponentName.unflattenFromString(stackInfo.taskNames[length]);
                if (unflattenFromString != null && !unflattenFromString.getPackageName().equals(packageName)) {
                    return unflattenFromString;
                }
            }
            return null;
        } catch (Exception unused) {
            Log.w("PipUtils", "Unable to get pinned stack.");
            return null;
        }
    }
}
