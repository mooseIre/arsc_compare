package com.android.systemui.media;

import com.android.systemui.media.SeekBarViewModel;

/* compiled from: SeekBarViewModel.kt */
final class SeekBarViewModel$onSeekProgress$1 implements Runnable {
    final /* synthetic */ long $position;
    final /* synthetic */ SeekBarViewModel this$0;

    SeekBarViewModel$onSeekProgress$1(SeekBarViewModel seekBarViewModel, long j) {
        this.this$0 = seekBarViewModel;
        this.$position = j;
    }

    public final void run() {
        if (this.this$0.scrubbing) {
            SeekBarViewModel seekBarViewModel = this.this$0;
            seekBarViewModel.set_data(SeekBarViewModel.Progress.copy$default(seekBarViewModel._data, false, false, Integer.valueOf((int) this.$position), (Integer) null, 11, (Object) null));
        }
    }
}
