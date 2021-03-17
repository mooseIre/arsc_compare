package com.android.systemui.media;

/* access modifiers changed from: package-private */
/* compiled from: SeekBarViewModel.kt */
public final class SeekBarViewModel$onDestroy$1 implements Runnable {
    final /* synthetic */ SeekBarViewModel this$0;

    SeekBarViewModel$onDestroy$1(SeekBarViewModel seekBarViewModel) {
        this.this$0 = seekBarViewModel;
    }

    public final void run() {
        this.this$0.setController(null);
        this.this$0.playbackState = null;
        Runnable runnable = this.this$0.cancel;
        if (runnable != null) {
            runnable.run();
        }
        this.this$0.cancel = null;
    }
}
