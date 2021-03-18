package com.android.systemui.media;

import android.util.Log;
import com.android.systemui.media.MediaTimeoutListener;

/* access modifiers changed from: package-private */
/* compiled from: MediaTimeoutListener.kt */
public final class MediaTimeoutListener$PlaybackStateListener$processState$1 implements Runnable {
    final /* synthetic */ boolean $dispatchEvents;
    final /* synthetic */ MediaTimeoutListener.PlaybackStateListener this$0;

    MediaTimeoutListener$PlaybackStateListener$processState$1(MediaTimeoutListener.PlaybackStateListener playbackStateListener, boolean z) {
        this.this$0 = playbackStateListener;
        this.$dispatchEvents = z;
    }

    public final void run() {
        this.this$0.cancellation = null;
        Log.v("MediaTimeout", "Execute timeout for " + this.this$0.key);
        this.this$0.setTimedOut(true);
        if (this.$dispatchEvents) {
            this.this$0.this$0.getTimeoutCallback().invoke(this.this$0.key, Boolean.valueOf(this.this$0.getTimedOut()));
        }
    }
}
