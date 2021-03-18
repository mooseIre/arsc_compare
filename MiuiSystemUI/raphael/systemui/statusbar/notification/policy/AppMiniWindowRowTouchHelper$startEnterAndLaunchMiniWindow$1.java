package com.android.systemui.statusbar.notification.policy;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Lambda;

/* access modifiers changed from: package-private */
/* compiled from: AppMiniWindowRowTouchHelper.kt */
public final class AppMiniWindowRowTouchHelper$startEnterAndLaunchMiniWindow$1 extends Lambda implements Function0<Unit> {
    final /* synthetic */ AppMiniWindowRowTouchHelper this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    AppMiniWindowRowTouchHelper$startEnterAndLaunchMiniWindow$1(AppMiniWindowRowTouchHelper appMiniWindowRowTouchHelper) {
        super(0);
        this.this$0 = appMiniWindowRowTouchHelper;
    }

    @Override // kotlin.jvm.functions.Function0
    public final void invoke() {
        if (this.this$0.mHandler.hasMessages(1)) {
            this.this$0.mHandler.removeMessages(1);
            this.this$0.mHandler.sendEmptyMessage(1);
        }
    }
}
