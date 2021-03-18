package com.android.systemui.shared.system;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.AppGlobals;
import android.content.pm.UserInfo;
import android.graphics.Rect;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import java.util.List;
import java.util.concurrent.Future;

public class ActivityManagerWrapper {
    private static final ActivityManagerWrapper sInstance = new ActivityManagerWrapper();
    private final BackgroundExecutor mBackgroundExecutor = BackgroundExecutor.get();
    private final TaskStackChangeListeners mTaskStackChangeListeners = new TaskStackChangeListeners(Looper.getMainLooper());

    private ActivityManagerWrapper() {
        AppGlobals.getInitialApplication().getPackageManager();
    }

    public static ActivityManagerWrapper getInstance() {
        return sInstance;
    }

    public int getCurrentUserId() {
        try {
            UserInfo currentUser = ActivityManager.getService().getCurrentUser();
            if (currentUser != null) {
                return currentUser.id;
            }
            return 0;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public ActivityManager.RunningTaskInfo getRunningTask() {
        return getRunningTask(false);
    }

    public ActivityManager.RunningTaskInfo getRunningTask(boolean z) {
        try {
            List filteredTasks = ActivityTaskManager.getService().getFilteredTasks(1, z);
            if (filteredTasks.isEmpty()) {
                return null;
            }
            return (ActivityManager.RunningTaskInfo) filteredTasks.get(0);
        } catch (RemoteException unused) {
            return null;
        }
    }

    public boolean setTaskWindowingModeSplitScreenPrimary(int i, int i2, Rect rect) {
        try {
            return ActivityTaskManager.getService().setTaskWindowingModeSplitScreenPrimary(i, true);
        } catch (RemoteException unused) {
            return false;
        }
    }

    public void registerTaskStackListener(TaskStackChangeListener taskStackChangeListener) {
        synchronized (this.mTaskStackChangeListeners) {
            this.mTaskStackChangeListeners.addListener(ActivityManager.getService(), taskStackChangeListener);
        }
    }

    public void unregisterTaskStackListener(TaskStackChangeListener taskStackChangeListener) {
        synchronized (this.mTaskStackChangeListeners) {
            this.mTaskStackChangeListeners.removeListener(taskStackChangeListener);
        }
    }

    public Future<?> closeSystemWindows(final String str) {
        return this.mBackgroundExecutor.submit(new Runnable(this) {
            /* class com.android.systemui.shared.system.ActivityManagerWrapper.AnonymousClass7 */

            public void run() {
                try {
                    ActivityManager.getService().closeSystemDialogs(str);
                } catch (RemoteException e) {
                    Log.w("ActivityManagerWrapper", "Failed to close system windows", e);
                }
            }
        });
    }

    public boolean isScreenPinningActive() {
        try {
            return ActivityTaskManager.getService().getLockTaskModeState() == 2;
        } catch (RemoteException unused) {
            return false;
        }
    }

    public boolean isLockTaskKioskModeActive() {
        try {
            return ActivityTaskManager.getService().getLockTaskModeState() == 1;
        } catch (RemoteException unused) {
            return false;
        }
    }
}
