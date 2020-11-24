package com.android.systemui.plugins;

import android.app.PendingIntent;
import android.content.Intent;
import android.view.View;
import com.android.systemui.plugins.annotations.ProvidesInterface;

@ProvidesInterface(version = 2)
public interface ActivityStarter {
    public static final int VERSION = 2;

    public interface Callback {
        void onActivityStarted(int i);
    }

    public interface OnDismissAction {
        boolean onDismiss();
    }

    void dismissKeyguardThenExecute(OnDismissAction onDismissAction, Runnable runnable, boolean z);

    void postQSRunnableDismissingKeyguard(Runnable runnable);

    void postStartActivityDismissingKeyguard(PendingIntent pendingIntent);

    void postStartActivityDismissingKeyguard(Intent intent, int i);

    void startActivity(Intent intent, boolean z);

    void startActivity(Intent intent, boolean z, Callback callback);

    void startActivity(Intent intent, boolean z, boolean z2);

    void startActivity(Intent intent, boolean z, boolean z2, int i);

    void startPendingIntentDismissingKeyguard(PendingIntent pendingIntent);

    void startPendingIntentDismissingKeyguard(PendingIntent pendingIntent, Runnable runnable);

    void startPendingIntentDismissingKeyguard(PendingIntent pendingIntent, Runnable runnable, View view);
}
