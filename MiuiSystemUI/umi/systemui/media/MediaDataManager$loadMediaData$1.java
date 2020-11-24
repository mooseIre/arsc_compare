package com.android.systemui.media;

import android.service.notification.StatusBarNotification;

/* compiled from: MediaDataManager.kt */
final class MediaDataManager$loadMediaData$1 implements Runnable {
    final /* synthetic */ String $key;
    final /* synthetic */ String $oldKey;
    final /* synthetic */ StatusBarNotification $sbn;
    final /* synthetic */ MediaDataManager this$0;

    MediaDataManager$loadMediaData$1(MediaDataManager mediaDataManager, String str, StatusBarNotification statusBarNotification, String str2) {
        this.this$0 = mediaDataManager;
        this.$key = str;
        this.$sbn = statusBarNotification;
        this.$oldKey = str2;
    }

    public final void run() {
        this.this$0.loadMediaDataInBg(this.$key, this.$sbn, this.$oldKey);
    }
}
