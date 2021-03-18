package com.android.systemui.controls;

import android.view.animation.AccelerateInterpolator;

/* access modifiers changed from: package-private */
/* compiled from: TooltipManager.kt */
public final class TooltipManager$hide$1 implements Runnable {
    final /* synthetic */ boolean $animate;
    final /* synthetic */ TooltipManager this$0;

    TooltipManager$hide$1(TooltipManager tooltipManager, boolean z) {
        this.this$0 = tooltipManager;
        this.$animate = z;
    }

    public final void run() {
        if (this.$animate) {
            this.this$0.getLayout().animate().alpha(0.0f).withLayer().setStartDelay(0).setDuration(100).setInterpolator(new AccelerateInterpolator()).start();
            return;
        }
        this.this$0.getLayout().animate().cancel();
        this.this$0.getLayout().setAlpha(0.0f);
    }
}
