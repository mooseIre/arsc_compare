package com.android.systemui.media;

import com.android.systemui.tuner.TunerService;
import com.android.systemui.util.Utils;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaResumeListener.kt */
public final class MediaResumeListener$setManager$1 implements TunerService.Tunable {
    final /* synthetic */ MediaResumeListener this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    MediaResumeListener$setManager$1(MediaResumeListener mediaResumeListener) {
        this.this$0 = mediaResumeListener;
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(@Nullable String str, @Nullable String str2) {
        MediaResumeListener mediaResumeListener = this.this$0;
        mediaResumeListener.useMediaResumption = Utils.useMediaResumption(mediaResumeListener.context);
        MediaResumeListener.access$getMediaDataManager$p(this.this$0).setMediaResumptionEnabled(this.this$0.useMediaResumption);
    }
}
