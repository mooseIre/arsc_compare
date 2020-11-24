package com.android.systemui.statusbar.phone;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Lambda;

/* compiled from: MiuiHeadsUpTouchHelper.kt */
final class MiuiHeadsUpTouchHelper$startEnterAndLaunchMiniWindow$1 extends Lambda implements Function0<Unit> {
    final /* synthetic */ MiuiHeadsUpTouchHelper this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    MiuiHeadsUpTouchHelper$startEnterAndLaunchMiniWindow$1(MiuiHeadsUpTouchHelper miuiHeadsUpTouchHelper) {
        super(0);
        this.this$0 = miuiHeadsUpTouchHelper;
    }

    public final void invoke() {
        if (this.this$0.mHandler.hasMessages(1)) {
            this.this$0.mHandler.removeMessages(1);
            this.this$0.mHandler.sendEmptyMessage(1);
        }
    }
}
