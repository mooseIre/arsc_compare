package com.android.systemui.qs;

import java.util.function.Consumer;

/* access modifiers changed from: package-private */
/* compiled from: MiuiQSContainer.kt */
public final class MiuiQSContainer$addQSContent$2<T> implements Consumer<Boolean> {
    final /* synthetic */ MiuiQSContainer this$0;

    MiuiQSContainer$addQSContent$2(MiuiQSContainer miuiQSContainer) {
        this.this$0 = miuiQSContainer;
    }

    public final void accept(Boolean bool) {
        QSPanel qsPanel = this.this$0.getQsPanel();
        if (qsPanel != null && qsPanel.isShown()) {
            this.this$0.animateBottomOnNextLayout = true;
        }
    }
}
