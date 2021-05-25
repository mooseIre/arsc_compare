package com.android.systemui.qs;

import java.util.function.Consumer;

/* access modifiers changed from: package-private */
/* compiled from: MiuiQSContainer.kt */
public final class MiuiQSContainer$addQSContent$1<T> implements Consumer<Boolean> {
    final /* synthetic */ MiuiQSContainer this$0;

    MiuiQSContainer$addQSContent$1(MiuiQSContainer miuiQSContainer) {
        this.this$0 = miuiQSContainer;
    }

    public final void accept(Boolean bool) {
        QuickQSPanel quickQSPanel = this.this$0.getQuickQSPanel();
        if (quickQSPanel != null && quickQSPanel.isShown()) {
            this.this$0.animateBottomOnNextLayout = true;
        }
    }
}
