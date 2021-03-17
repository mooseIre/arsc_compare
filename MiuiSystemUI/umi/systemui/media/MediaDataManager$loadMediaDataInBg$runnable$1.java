package com.android.systemui.media;

import android.app.Notification;
import android.app.PendingIntent;
import android.util.Log;

/* access modifiers changed from: package-private */
/* compiled from: MediaDataManager.kt */
public final class MediaDataManager$loadMediaDataInBg$runnable$1 implements Runnable {
    final /* synthetic */ Notification.Action $action;

    MediaDataManager$loadMediaDataInBg$runnable$1(Notification.Action action) {
        this.$action = action;
    }

    public final void run() {
        try {
            this.$action.actionIntent.send();
        } catch (PendingIntent.CanceledException e) {
            Log.d("MediaDataManager", "Intent canceled", e);
        }
    }
}
