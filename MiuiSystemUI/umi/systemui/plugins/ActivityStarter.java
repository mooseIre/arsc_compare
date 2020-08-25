package com.android.systemui.plugins;

import android.app.PendingIntent;
import android.content.Intent;
import com.android.systemui.plugins.annotations.ProvidesInterface;

@ProvidesInterface(version = 1)
public interface ActivityStarter {
    public static final int VERSION = 1;

    public interface Callback {
        void onActivityStarted(int i);
    }

    public interface OnDismissAction {
        boolean onDismiss();
    }

    void collapsePanels();

    void dismissKeyguardThenExecute(OnDismissAction onDismissAction, Runnable runnable, boolean z);

    void postQSRunnableDismissingKeyguard(Runnable runnable);

    void postStartActivityDismissingKeyguard(PendingIntent pendingIntent);

    void postStartActivityDismissingKeyguard(Intent intent, int i);

    void startActivity(Intent intent, boolean z);

    void startActivity(Intent intent, boolean z, Callback callback);

    void startActivity(Intent intent, boolean z, boolean z2);

    void startActivity(Intent intent, boolean z, boolean z2, int i);

    void startPendingIntentDismissingKeyguard(PendingIntent pendingIntent);
}
