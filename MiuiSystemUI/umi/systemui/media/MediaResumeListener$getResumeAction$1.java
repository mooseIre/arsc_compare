package com.android.systemui.media;

import android.content.ComponentName;
import android.content.Context;
import android.media.session.MediaController;
import android.util.Log;
import com.android.systemui.media.ResumeMediaBrowser;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: MediaResumeListener.kt */
final class MediaResumeListener$getResumeAction$1 implements Runnable {
    final /* synthetic */ ComponentName $componentName;
    final /* synthetic */ MediaResumeListener this$0;

    MediaResumeListener$getResumeAction$1(MediaResumeListener mediaResumeListener, ComponentName componentName) {
        this.this$0 = mediaResumeListener;
        this.$componentName = componentName;
    }

    public final void run() {
        ResumeMediaBrowser access$getMediaBrowser$p = this.this$0.mediaBrowser;
        if (access$getMediaBrowser$p != null) {
            access$getMediaBrowser$p.disconnect();
        }
        MediaResumeListener mediaResumeListener = this.this$0;
        mediaResumeListener.mediaBrowser = new ResumeMediaBrowser(mediaResumeListener.context, new ResumeMediaBrowser.Callback(this) {
            final /* synthetic */ MediaResumeListener$getResumeAction$1 this$0;

            {
                this.this$0 = r1;
            }

            public void onConnected() {
                ResumeMediaBrowser access$getMediaBrowser$p = this.this$0.this$0.mediaBrowser;
                if ((access$getMediaBrowser$p != null ? access$getMediaBrowser$p.getToken() : null) == null) {
                    Log.e("MediaResumeListener", "Error after connect");
                    ResumeMediaBrowser access$getMediaBrowser$p2 = this.this$0.this$0.mediaBrowser;
                    if (access$getMediaBrowser$p2 != null) {
                        access$getMediaBrowser$p2.disconnect();
                    }
                    this.this$0.this$0.mediaBrowser = null;
                    return;
                }
                Log.d("MediaResumeListener", "Connected for restart " + this.this$0.$componentName);
                Context access$getContext$p = this.this$0.this$0.context;
                ResumeMediaBrowser access$getMediaBrowser$p3 = this.this$0.this$0.mediaBrowser;
                if (access$getMediaBrowser$p3 != null) {
                    MediaController.TransportControls transportControls = new MediaController(access$getContext$p, access$getMediaBrowser$p3.getToken()).getTransportControls();
                    transportControls.prepare();
                    transportControls.play();
                    return;
                }
                Intrinsics.throwNpe();
                throw null;
            }

            public void onError() {
                Log.e("MediaResumeListener", "Resume failed for " + this.this$0.$componentName);
                ResumeMediaBrowser access$getMediaBrowser$p = this.this$0.this$0.mediaBrowser;
                if (access$getMediaBrowser$p != null) {
                    access$getMediaBrowser$p.disconnect();
                }
                this.this$0.this$0.mediaBrowser = null;
            }
        }, this.$componentName);
        ResumeMediaBrowser access$getMediaBrowser$p2 = this.this$0.mediaBrowser;
        if (access$getMediaBrowser$p2 != null) {
            access$getMediaBrowser$p2.restart();
        }
    }
}
