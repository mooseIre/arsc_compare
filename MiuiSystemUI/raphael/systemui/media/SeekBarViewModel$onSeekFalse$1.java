package com.android.systemui.media;

/* access modifiers changed from: package-private */
/* compiled from: SeekBarViewModel.kt */
public final class SeekBarViewModel$onSeekFalse$1 implements Runnable {
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
