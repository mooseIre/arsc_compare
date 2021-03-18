package com.android.systemui.media;

/* access modifiers changed from: package-private */
/* compiled from: SeekBarViewModel.kt */
public final class SeekBarViewModel$listening$1 implements Runnable {
    final /* synthetic */ boolean $value;
    final /* synthetic */ SeekBarViewModel this$0;

    SeekBarViewModel$listening$1(SeekBarViewModel seekBarViewModel, boolean z) {
        this.this$0 = seekBarViewModel;
        this.$value = z;
    }

    public final void run() {
        boolean z = this.this$0.listening;
        boolean z2 = this.$value;
        if (z != z2) {
            this.this$0.listening = z2;
            this.this$0.checkIfPollingNeeded();
        }
    }
}
