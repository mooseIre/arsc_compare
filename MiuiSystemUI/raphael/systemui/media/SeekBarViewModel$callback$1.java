package com.android.systemui.media;

import android.media.session.MediaController;
import android.media.session.PlaybackState;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: SeekBarViewModel.kt */
public final class SeekBarViewModel$callback$1 extends MediaController.Callback {
    final /* synthetic */ SeekBarViewModel this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    SeekBarViewModel$callback$1(SeekBarViewModel seekBarViewModel) {
        this.this$0 = seekBarViewModel;
    }

    public void onPlaybackStateChanged(@NotNull PlaybackState playbackState) {
        Intrinsics.checkParameterIsNotNull(playbackState, "state");
        this.this$0.playbackState = playbackState;
        Integer num = 0;
        if (num.equals(this.this$0.playbackState)) {
            this.this$0.clearController();
        } else {
            this.this$0.checkIfPollingNeeded();
        }
    }

    public void onSessionDestroyed() {
        this.this$0.clearController();
    }
}
