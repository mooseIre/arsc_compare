package com.android.systemui.shared.system;

import android.app.ActivityManager;
import android.os.Looper;

public class ActivityManagerWrapper {
    private static final ActivityManagerWrapper sInstance = new ActivityManagerWrapper();
    private final TaskStackChangeListeners mTaskStackChangeListeners = new TaskStackChangeListeners(Looper.getMainLooper());

    private ActivityManagerWrapper() {
    }

    public static ActivityManagerWrapper getInstance() {
        return sInstance;
    }

    public void registerTaskStackListener(TaskStackChangeListener taskStackChangeListener) {
        synchronized (this.mTaskStackChangeListeners) {
            this.mTaskStackChangeListeners.addListener(ActivityManager.getService(), taskStackChangeListener);
        }
    }
}
