package com.android.systemui.media;

import android.app.PendingIntent;
import android.media.MediaDescription;
import android.media.session.MediaSession;

/* compiled from: MediaDataManager.kt */
final class MediaDataManager$addResumptionControls$1 implements Runnable {
    final /* synthetic */ Runnable $action;
    final /* synthetic */ PendingIntent $appIntent;
    final /* synthetic */ String $appName;
    final /* synthetic */ MediaDescription $desc;
    final /* synthetic */ String $packageName;
    final /* synthetic */ MediaSession.Token $token;
    final /* synthetic */ int $userId;
    final /* synthetic */ MediaDataManager this$0;

    MediaDataManager$addResumptionControls$1(MediaDataManager mediaDataManager, int i, MediaDescription mediaDescription, Runnable runnable, MediaSession.Token token, String str, PendingIntent pendingIntent, String str2) {
        this.this$0 = mediaDataManager;
        this.$userId = i;
        this.$desc = mediaDescription;
        this.$action = runnable;
        this.$token = token;
        this.$appName = str;
        this.$appIntent = pendingIntent;
        this.$packageName = str2;
    }

    public final void run() {
        this.this$0.loadMediaDataInBgForResumption(this.$userId, this.$desc, this.$action, this.$token, this.$appName, this.$appIntent, this.$packageName);
    }
}
