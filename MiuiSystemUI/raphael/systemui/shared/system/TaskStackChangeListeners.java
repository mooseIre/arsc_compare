package com.android.systemui.shared.system;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.IActivityManager;
import android.app.TaskStackListener;
import android.content.ComponentName;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class TaskStackChangeListeners extends TaskStackListener {
    private static final String TAG = "TaskStackChangeListeners";
    private final Handler mHandler;
    private boolean mRegistered;
    /* access modifiers changed from: private */
    public final List<TaskStackChangeListener> mTaskStackListeners = new ArrayList();
    private final List<TaskStackChangeListener> mTmpListeners = new ArrayList();

    public TaskStackChangeListeners(Looper looper) {
        this.mHandler = new H(looper);
    }

    public void addListener(IActivityManager iActivityManager, TaskStackChangeListener taskStackChangeListener) {
        this.mTaskStackListeners.add(taskStackChangeListener);
        if (!this.mRegistered) {
            try {
                ActivityTaskManager.getService().registerTaskStackListener(this);
                this.mRegistered = true;
            } catch (Exception e) {
                Log.w(TAG, "Failed to call registerTaskStackListener", e);
            }
        }
    }

    public void onTaskStackChanged() throws RemoteException {
        synchronized (this.mTaskStackListeners) {
            this.mTmpListeners.clear();
            this.mTmpListeners.addAll(this.mTaskStackListeners);
        }
        for (int size = this.mTmpListeners.size() - 1; size >= 0; size--) {
            this.mTmpListeners.get(size).onTaskStackChangedBackground();
        }
        this.mHandler.removeMessages(1);
        this.mHandler.sendEmptyMessage(1);
    }

    public void onActivityPinned(String str, int i, int i2, int i3) throws RemoteException {
        this.mHandler.removeMessages(3);
        this.mHandler.obtainMessage(3, new PinnedActivityInfo(str, i, i2, i3)).sendToTarget();
    }

    public void onActivityUnpinned() throws RemoteException {
        this.mHandler.removeMessages(10);
        this.mHandler.sendEmptyMessage(10);
    }

    public void onPinnedActivityRestartAttempt(boolean z) throws RemoteException {
        this.mHandler.removeMessages(4);
        this.mHandler.obtainMessage(4, z ? 1 : 0, 0).sendToTarget();
    }

    public void onPinnedStackAnimationStarted() throws RemoteException {
        this.mHandler.removeMessages(9);
        this.mHandler.sendEmptyMessage(9);
    }

    public void onPinnedStackAnimationEnded() throws RemoteException {
        this.mHandler.removeMessages(5);
        this.mHandler.sendEmptyMessage(5);
    }

    public void onActivityForcedResizable(String str, int i, int i2) throws RemoteException {
        this.mHandler.obtainMessage(6, i, i2, str).sendToTarget();
    }

    public void onActivityDismissingDockedStack() throws RemoteException {
        this.mHandler.sendEmptyMessage(7);
    }

    public void onActivityLaunchOnSecondaryDisplayFailed(ActivityManager.RunningTaskInfo runningTaskInfo, int i) throws RemoteException {
        this.mHandler.obtainMessage(11, i, 0, runningTaskInfo).sendToTarget();
    }

    public void onActivityLaunchOnSecondaryDisplayRerouted(ActivityManager.RunningTaskInfo runningTaskInfo, int i) throws RemoteException {
        this.mHandler.obtainMessage(16, i, 0, runningTaskInfo).sendToTarget();
    }

    public void onTaskProfileLocked(int i, int i2) throws RemoteException {
        this.mHandler.obtainMessage(8, i, i2).sendToTarget();
    }

    public void onTaskSnapshotChanged(int i, ActivityManager.TaskSnapshot taskSnapshot) throws RemoteException {
        this.mHandler.obtainMessage(2, i, 0, taskSnapshot).sendToTarget();
    }

    public void onTaskCreated(int i, ComponentName componentName) throws RemoteException {
        this.mHandler.obtainMessage(12, i, 0, componentName).sendToTarget();
    }

    public void onTaskRemoved(int i) throws RemoteException {
        this.mHandler.obtainMessage(13, i, 0).sendToTarget();
    }

    public void onTaskMovedToFront(ActivityManager.RunningTaskInfo runningTaskInfo) throws RemoteException {
        this.mHandler.obtainMessage(14, runningTaskInfo).sendToTarget();
    }

    public void onBackPressedOnTaskRoot(ActivityManager.RunningTaskInfo runningTaskInfo) throws RemoteException {
        this.mHandler.obtainMessage(18, runningTaskInfo).sendToTarget();
    }

    public void onActivityRequestedOrientationChanged(int i, int i2) throws RemoteException {
        this.mHandler.obtainMessage(15, i, i2).sendToTarget();
    }

    public void onSizeCompatModeActivityChanged(int i, IBinder iBinder) {
        this.mHandler.obtainMessage(17, i, 0, iBinder).sendToTarget();
    }

    public void onTaskDisplayChanged(int i, int i2) throws RemoteException {
        this.mHandler.obtainMessage(19, i, i2).sendToTarget();
    }

    private final class H extends Handler {
        public H(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            synchronized (TaskStackChangeListeners.this.mTaskStackListeners) {
                int i = message.what;
                if (i == 14) {
                    ActivityManager.RunningTaskInfo runningTaskInfo = (ActivityManager.RunningTaskInfo) message.obj;
                    for (int size = TaskStackChangeListeners.this.mTaskStackListeners.size() - 1; size >= 0; size--) {
                        ((TaskStackChangeListener) TaskStackChangeListeners.this.mTaskStackListeners.get(size)).onTaskMovedToFront(runningTaskInfo);
                    }
                } else if (i == 16) {
                    ActivityManager.RunningTaskInfo runningTaskInfo2 = (ActivityManager.RunningTaskInfo) message.obj;
                    for (int size2 = TaskStackChangeListeners.this.mTaskStackListeners.size() - 1; size2 >= 0; size2--) {
                        ((TaskStackChangeListener) TaskStackChangeListeners.this.mTaskStackListeners.get(size2)).onActivityLaunchOnSecondaryDisplayRerouted(runningTaskInfo2);
                    }
                } else if (i == 18) {
                    for (int size3 = TaskStackChangeListeners.this.mTaskStackListeners.size() - 1; size3 >= 0; size3--) {
                        ((TaskStackChangeListener) TaskStackChangeListeners.this.mTaskStackListeners.get(size3)).onBackPressedOnTaskRoot((ActivityManager.RunningTaskInfo) message.obj);
                    }
                }
            }
        }
    }

    private static class PinnedActivityInfo {
        final String mPackageName;
        final int mStackId;
        final int mTaskId;
        final int mUserId;

        PinnedActivityInfo(String str, int i, int i2, int i3) {
            this.mPackageName = str;
            this.mUserId = i;
            this.mTaskId = i2;
            this.mStackId = i3;
        }
    }
}
