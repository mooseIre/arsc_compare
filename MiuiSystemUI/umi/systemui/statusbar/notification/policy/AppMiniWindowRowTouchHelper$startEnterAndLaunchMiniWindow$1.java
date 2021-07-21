package com.android.systemui.statusbar.notification.policy;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Lambda;

/* compiled from: AppMiniWindowRowTouchHelper.kt */
final class AppMiniWindowRowTouchHelper$startEnterAndLaunchMiniWindow$1 extends Lambda implements Function0<Unit> {
    final /* synthetic */ AppMiniWindowRowTouchHelper this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    AppMiniWindowRowTouchHelper$startEnterAndLaunchMiniWindow$1(AppMiniWindowRowTouchHelper appMiniWindowRowTouchHelper) {
        super(0);
        this.this$0 = appMiniWindowRowTouchHelper;
    }

    @Override // kotlin.jvm.functions.Function0
    public final void invoke() {
        if (AppMiniWindowRowTouchHelper.access$getMHandler$p(this.this$0).hasMessages(1)) {
            AppMiniWindowRowTouchHelper.access$getMHandler$p(this.this$0).removeMessages(1);
            AppMiniWindowRowTouchHelper.access$getMHandler$p(this.this$0).sendEmptyMessage(1);
        }
    }
}
