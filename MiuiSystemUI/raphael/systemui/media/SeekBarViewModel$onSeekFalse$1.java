package com.android.systemui.media;

/* compiled from: SeekBarViewModel.kt */
final class SeekBarViewModel$onSeekFalse$1 implements Runnable {
    final /* synthetic */ SeekBarViewModel this$0;

    SeekBarViewModel$onSeekFalse$1(SeekBarViewModel seekBarViewModel) {
        this.this$0 = seekBarViewModel;
    }

    public final void run() {
        if (this.this$0.scrubbing) {
            this.this$0.isFalseSeek = true;
        }
    }
}
