package com.android.systemui.pip;

import android.app.ActivityManager;
import android.app.IActivityManager;
import android.os.RemoteException;
import java.util.List;

public class PipUIHelper {
    public static List<ActivityManager.RunningTaskInfo> getTasks(IActivityManager iActivityManager, int i, int i2) {
        try {
            return iActivityManager.getTasks(i);
        } catch (RemoteException unused) {
            return null;
        }
    }
}
