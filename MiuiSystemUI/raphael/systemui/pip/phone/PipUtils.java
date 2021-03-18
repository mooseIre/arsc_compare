package com.android.systemui.pip.phone;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.IActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import android.util.Pair;

public class PipUtils {
    public static Pair<ComponentName, Integer> getTopPipActivity(Context context, IActivityManager iActivityManager) {
        try {
            String packageName = context.getPackageName();
            ActivityManager.StackInfo stackInfo = ActivityTaskManager.getService().getStackInfo(2, 0);
            if (!(stackInfo == null || stackInfo.taskIds == null || stackInfo.taskIds.length <= 0)) {
                for (int length = stackInfo.taskNames.length - 1; length >= 0; length--) {
                    ComponentName unflattenFromString = ComponentName.unflattenFromString(stackInfo.taskNames[length]);
                    if (!(unflattenFromString == null || unflattenFromString.getPackageName().equals(packageName))) {
                        return new Pair<>(unflattenFromString, Integer.valueOf(stackInfo.taskUserIds[length]));
                    }
                }
            }
        } catch (RemoteException unused) {
            Log.w("PipUtils", "Unable to get pinned stack.");
        }
        return new Pair<>(null, 0);
    }
}
