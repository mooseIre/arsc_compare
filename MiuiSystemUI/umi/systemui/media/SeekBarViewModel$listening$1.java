package com.android.systemui.media;

/* compiled from: SeekBarViewModel.kt */
final class SeekBarViewModel$listening$1 implements Runnable {
    final /* synthetic */ boolean $value;
    final /* synthetic */ SeekBarViewModel this$0;

    SeekBarViewModel$listening$1(SeekBarViewModel seekBarViewModel, boolean z) {
        this.this$0 = seekBarViewModel;
        this.$value = z;
    }

    public final void run() {
        boolean access$getListening$lp = this.this$0.listening;
        boolean z = this.$value;
        if (access$getListening$lp != z) {
            this.this$0.listening = z;
            this.this$0.checkIfPollingNeeded();
        }
    }
}
