package com.android.systemui.recents.misc;

import android.app.ActivityManager;
import android.app.ITaskStackListener;
import android.content.ComponentName;
import android.os.IBinder;
import android.os.RemoteException;

public abstract class TaskStackListener extends ITaskStackListener.Stub {
    public void onActivityLaunchOnSecondaryDisplayFailed(ActivityManager.RunningTaskInfo runningTaskInfo, int i) throws RemoteException {
    }

    public void onActivityLaunchOnSecondaryDisplayRerouted(ActivityManager.RunningTaskInfo runningTaskInfo, int i) throws RemoteException {
    }

    public void onActivityRequestedOrientationChanged(int i, int i2) throws RemoteException {
    }

    public void onActivityRotation(int i) throws RemoteException {
    }

    public void onBackPressedOnTaskRoot(ActivityManager.RunningTaskInfo runningTaskInfo) throws RemoteException {
    }

    public void onRecentTaskListFrozenChanged(boolean z) throws RemoteException {
    }

    public void onRecentTaskListUpdated() throws RemoteException {
    }

    public void onSingleTaskDisplayDrawn(int i) throws RemoteException {
    }

    public void onSingleTaskDisplayEmpty(int i) throws RemoteException {
    }

    public void onSizeCompatModeActivityChanged(int i, IBinder iBinder) throws RemoteException {
    }

    public void onTaskCreated(int i, ComponentName componentName) throws RemoteException {
    }

    public void onTaskDescriptionChanged(ActivityManager.RunningTaskInfo runningTaskInfo) {
    }

    public void onTaskDisplayChanged(int i, int i2) throws RemoteException {
    }

    public void onTaskFocusChanged(int i, boolean z) throws RemoteException {
    }

    public void onTaskMovedToFront(ActivityManager.RunningTaskInfo runningTaskInfo) throws RemoteException {
    }

    public void onTaskRemovalStarted(ActivityManager.RunningTaskInfo runningTaskInfo) throws RemoteException {
    }

    public void onTaskRemoved(int i) throws RemoteException {
    }

    public void onTaskRequestedOrientationChanged(int i, int i2) throws RemoteException {
    }
}
