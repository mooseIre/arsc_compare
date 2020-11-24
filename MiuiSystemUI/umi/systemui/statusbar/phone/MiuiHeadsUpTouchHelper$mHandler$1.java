package com.android.systemui.statusbar.phone;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiHeadsUpTouchHelper.kt */
public final class MiuiHeadsUpTouchHelper$mHandler$1 extends Handler {
    final /* synthetic */ MiuiHeadsUpTouchHelper this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    MiuiHeadsUpTouchHelper$mHandler$1(MiuiHeadsUpTouchHelper miuiHeadsUpTouchHelper, Looper looper) {
        super(looper);
        this.this$0 = miuiHeadsUpTouchHelper;
    }

    public void handleMessage(@NotNull Message message) {
        Intrinsics.checkParameterIsNotNull(message, "msg");
        super.handleMessage(message);
        if (message.what == 1) {
            this.this$0.handleHideNotificationPanel();
        }
    }
}
