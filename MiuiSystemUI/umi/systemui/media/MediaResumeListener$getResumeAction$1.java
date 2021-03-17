package com.android.systemui.media;

import android.content.ComponentName;
import android.content.Context;
import android.media.session.MediaController;
import android.util.Log;
import com.android.systemui.media.ResumeMediaBrowser;
import kotlin.jvm.internal.Intrinsics;

/* access modifiers changed from: package-private */
/* compiled from: MediaResumeListener.kt */
public final class MediaResumeListener$getResumeAction$1 implements Runnable {
    final /* synthetic */ ComponentName $componentName;
    final /* synthetic */ MediaResumeListener this$0;

    MediaResumeListener$getResumeAction$1(MediaResumeListener mediaResumeListener, ComponentName componentName) {
        this.this$0 = mediaResumeListener;
        this.$componentName = componentName;
    }

    public final void run() {
        ResumeMediaBrowser resumeMediaBrowser = this.this$0.mediaBrowser;
        if (resumeMediaBrowser != null) {
            resumeMediaBrowser.disconnect();
        }
        MediaResumeListener mediaResumeListener = this.this$0;
        mediaResumeListener.mediaBrowser = new ResumeMediaBrowser(mediaResumeListener.context, new ResumeMediaBrowser.Callback(this) {
            /* class com.android.systemui.media.MediaResumeListener$getResumeAction$1.AnonymousClass1 */
            final /* synthetic */ MediaResumeListener$getResumeAction$1 this$0;

            /* JADX WARN: Incorrect args count in method signature: ()V */
            {
                this.this$0 = r1;
            }

            @Override // com.android.systemui.media.ResumeMediaBrowser.Callback
            public void onConnected() {
                ResumeMediaBrowser resumeMediaBrowser = this.this$0.this$0.mediaBrowser;
                if ((resumeMediaBrowser != null ? resumeMediaBrowser.getToken() : null) == null) {
                    Log.e("MediaResumeListener", "Error after connect");
                    ResumeMediaBrowser resumeMediaBrowser2 = this.this$0.this$0.mediaBrowser;
                    if (resumeMediaBrowser2 != null) {
                        resumeMediaBrowser2.disconnect();
                    }
                    this.this$0.this$0.mediaBrowser = null;
                    return;
                }
                Log.d("MediaResumeListener", "Connected for restart " + this.this$0.$componentName);
                Context context = this.this$0.this$0.context;
                ResumeMediaBrowser resumeMediaBrowser3 = this.this$0.this$0.mediaBrowser;
                if (resumeMediaBrowser3 != null) {
                    MediaController.TransportControls transportControls = new MediaController(context, resumeMediaBrowser3.getToken()).getTransportControls();
                    transportControls.prepare();
                    transportControls.play();
                    return;
                }
                Intrinsics.throwNpe();
                throw null;
            }

            @Override // com.android.systemui.media.ResumeMediaBrowser.Callback
            public void onError() {
                Log.e("MediaResumeListener", "Resume failed for " + this.this$0.$componentName);
                ResumeMediaBrowser resumeMediaBrowser = this.this$0.this$0.mediaBrowser;
                if (resumeMediaBrowser != null) {
                    resumeMediaBrowser.disconnect();
                }
                this.this$0.this$0.mediaBrowser = null;
            }
        }, this.$componentName);
        ResumeMediaBrowser resumeMediaBrowser2 = this.this$0.mediaBrowser;
        if (resumeMediaBrowser2 != null) {
            resumeMediaBrowser2.restart();
        }
    }
}
