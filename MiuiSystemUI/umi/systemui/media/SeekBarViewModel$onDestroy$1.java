package com.android.systemui.media;

import android.media.session.MediaController;

/* compiled from: SeekBarViewModel.kt */
final class SeekBarViewModel$onDestroy$1 implements Runnable {
    final /* synthetic */ SeekBarViewModel this$0;

    SeekBarViewModel$onDestroy$1(SeekBarViewModel seekBarViewModel) {
        this.this$0 = seekBarViewModel;
    }

    public final void run() {
        this.this$0.setController((MediaController) null);
        this.this$0.playbackState = null;
        Runnable access$getCancel$p = this.this$0.cancel;
        if (access$getCancel$p != null) {
            access$getCancel$p.run();
        }
        this.this$0.cancel = null;
    }
}
