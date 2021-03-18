package com.android.systemui.media;

import com.android.systemui.media.SeekBarViewModel;

/* access modifiers changed from: package-private */
/* compiled from: SeekBarViewModel.kt */
public final class SeekBarViewModel$clearController$1 implements Runnable {
    final /* synthetic */ SeekBarViewModel this$0;

    SeekBarViewModel$clearController$1(SeekBarViewModel seekBarViewModel) {
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
        SeekBarViewModel seekBarViewModel = this.this$0;
        seekBarViewModel.set_data(SeekBarViewModel.Progress.copy$default(seekBarViewModel._data, false, false, null, null, 14, null));
    }
}
