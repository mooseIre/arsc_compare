package com.android.systemui.miui;

import android.content.ComponentName;
import android.content.Intent;
import com.android.systemui.statusbar.policy.CallbackController;

public interface ActivityObserver extends CallbackController<ActivityObserverCallback> {

    public interface ActivityObserverCallback {
        void activityDestroyed(Intent intent) {
        }

        void activityIdle(Intent intent) {
        }

        void activityPaused(Intent intent) {
        }

        void activityResumed(Intent intent) {
        }

        void activityStopped(Intent intent) {
        }

        void onLauncherPackageChanged(String str) {
        }
    }

    ComponentName getLastResumedActivity();

    String getLauncherPackage();

    ComponentName getTopActivity();

    boolean isTopActivityLauncher();
}
