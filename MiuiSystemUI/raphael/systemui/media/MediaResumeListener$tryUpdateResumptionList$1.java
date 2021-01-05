package com.android.systemui.media;

import android.content.ComponentName;
import android.util.Log;
import com.android.systemui.media.ResumeMediaBrowser;

/* compiled from: MediaResumeListener.kt */
public final class MediaResumeListener$tryUpdateResumptionList$1 extends ResumeMediaBrowser.Callback {
    final /* synthetic */ ComponentName $componentName;
    final /* synthetic */ String $key;
    final /* synthetic */ MediaResumeListener this$0;

    MediaResumeListener$tryUpdateResumptionList$1(MediaResumeListener mediaResumeListener, ComponentName componentName, String str) {
        this.this$0 = mediaResumeListener;
        this.$componentName = componentName;
        this.$key = str;
    }

    public void onConnected() {
        Log.d("MediaResumeListener", "yes we can resume with " + this.$componentName);
        MediaResumeListener.access$getMediaDataManager$p(this.this$0).setResumeAction(this.$key, this.this$0.getResumeAction(this.$componentName));
        this.this$0.updateResumptionList(this.$componentName);
        ResumeMediaBrowser access$getMediaBrowser$p = this.this$0.mediaBrowser;
        if (access$getMediaBrowser$p != null) {
            access$getMediaBrowser$p.disconnect();
        }
        this.this$0.mediaBrowser = null;
    }

    public void onError() {
        Log.e("MediaResumeListener", "Cannot resume with " + this.$componentName);
        MediaResumeListener.access$getMediaDataManager$p(this.this$0).setResumeAction(this.$key, (Runnable) null);
        ResumeMediaBrowser access$getMediaBrowser$p = this.this$0.mediaBrowser;
        if (access$getMediaBrowser$p != null) {
            access$getMediaBrowser$p.disconnect();
        }
        this.this$0.mediaBrowser = null;
    }
}
