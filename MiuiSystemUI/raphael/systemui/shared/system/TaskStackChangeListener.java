package com.android.systemui.shared.system;

import android.app.ActivityManager;

public abstract class TaskStackChangeListener {
    public void onActivityLaunchOnSecondaryDisplayRerouted() {
    }

    public void onBackPressedOnTaskRoot(ActivityManager.RunningTaskInfo runningTaskInfo) {
    }

    public void onTaskMovedToFront(int i) {
    }

    public void onTaskStackChangedBackground() {
    }

    public void onTaskMovedToFront(ActivityManager.RunningTaskInfo runningTaskInfo) {
        onTaskMovedToFront(runningTaskInfo.taskId);
    }

    public void onActivityLaunchOnSecondaryDisplayRerouted(ActivityManager.RunningTaskInfo runningTaskInfo) {
        onActivityLaunchOnSecondaryDisplayRerouted();
    }
}
