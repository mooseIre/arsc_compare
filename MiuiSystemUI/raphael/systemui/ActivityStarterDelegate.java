package com.android.systemui;

import android.app.PendingIntent;
import android.content.Intent;
import com.android.systemui.plugins.ActivityStarter;

public class ActivityStarterDelegate implements ActivityStarter {
    private ActivityStarter mActualStarter;

    public void startPendingIntentDismissingKeyguard(PendingIntent pendingIntent) {
        ActivityStarter activityStarter = this.mActualStarter;
        if (activityStarter != null) {
            activityStarter.startPendingIntentDismissingKeyguard(pendingIntent);
        }
    }

    public void startActivity(Intent intent, boolean z) {
        ActivityStarter activityStarter = this.mActualStarter;
        if (activityStarter != null) {
            activityStarter.startActivity(intent, z);
        }
    }

    public void startActivity(Intent intent, boolean z, boolean z2) {
        ActivityStarter activityStarter = this.mActualStarter;
        if (activityStarter != null) {
            activityStarter.startActivity(intent, z, z2);
        }
    }

    public void startActivity(Intent intent, boolean z, ActivityStarter.Callback callback) {
        ActivityStarter activityStarter = this.mActualStarter;
        if (activityStarter != null) {
            activityStarter.startActivity(intent, z, callback);
        }
    }

    public void postStartActivityDismissingKeyguard(Intent intent, int i) {
        ActivityStarter activityStarter = this.mActualStarter;
        if (activityStarter != null) {
            activityStarter.postStartActivityDismissingKeyguard(intent, i);
        }
    }

    public void postStartActivityDismissingKeyguard(PendingIntent pendingIntent) {
        ActivityStarter activityStarter = this.mActualStarter;
        if (activityStarter != null) {
            activityStarter.postStartActivityDismissingKeyguard(pendingIntent);
        }
    }

    public void postQSRunnableDismissingKeyguard(Runnable runnable) {
        ActivityStarter activityStarter = this.mActualStarter;
        if (activityStarter != null) {
            activityStarter.postQSRunnableDismissingKeyguard(runnable);
        }
    }

    public void dismissKeyguardThenExecute(ActivityStarter.OnDismissAction onDismissAction, Runnable runnable, boolean z) {
        ActivityStarter activityStarter = this.mActualStarter;
        if (activityStarter != null) {
            activityStarter.dismissKeyguardThenExecute(onDismissAction, runnable, z);
        }
    }

    public void collapsePanels() {
        ActivityStarter activityStarter = this.mActualStarter;
        if (activityStarter != null) {
            activityStarter.collapsePanels();
        }
    }

    public void setActivityStarterImpl(ActivityStarter activityStarter) {
        this.mActualStarter = activityStarter;
    }
}
