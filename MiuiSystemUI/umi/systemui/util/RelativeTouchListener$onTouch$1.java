package com.android.systemui.util;

import android.view.View;

/* compiled from: RelativeTouchListener.kt */
final class RelativeTouchListener$onTouch$1 implements Runnable {
    final /* synthetic */ View $v;
    final /* synthetic */ RelativeTouchListener this$0;

    RelativeTouchListener$onTouch$1(RelativeTouchListener relativeTouchListener, View view) {
        this.this$0 = relativeTouchListener;
        this.$v = view;
    }

    public final void run() {
        if (this.$v.isLongClickable()) {
            this.this$0.performedLongClick = this.$v.performLongClick();
        }
    }
}
