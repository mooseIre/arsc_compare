package com.android.systemui.media;

/* access modifiers changed from: package-private */
/* compiled from: SeekBarViewModel.kt */
public final class SeekBarViewModel$onSeekStarting$1 implements Runnable {
    final /* synthetic */ SeekBarViewModel this$0;

    SeekBarViewModel$onSeekStarting$1(SeekBarViewModel seekBarViewModel) {
        this.this$0 = seekBarViewModel;
    }

    public final void run() {
        this.this$0.setScrubbing(true);
        this.this$0.isFalseSeek = false;
    }
}
