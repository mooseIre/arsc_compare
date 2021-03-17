package com.android.systemui.media;

import com.android.systemui.media.MediaDeviceManager;

/* compiled from: MediaDeviceManager.kt */
final class MediaDeviceManager$Token$onDeviceListUpdate$1 implements Runnable {
    final /* synthetic */ MediaDeviceManager.Token this$0;

    MediaDeviceManager$Token$onDeviceListUpdate$1(MediaDeviceManager.Token token) {
        this.this$0 = token;
    }

    public final void run() {
        this.this$0.updateCurrent();
    }
}
