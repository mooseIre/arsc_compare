package com.android.systemui.statusbar.notification.policy;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: AppMiniWindowRowTouchHelper.kt */
public final class AppMiniWindowRowTouchHelper$mHandler$1 extends Handler {
    final /* synthetic */ AppMiniWindowRowTouchHelper this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    AppMiniWindowRowTouchHelper$mHandler$1(AppMiniWindowRowTouchHelper appMiniWindowRowTouchHelper, Looper looper) {
        super(looper);
        this.this$0 = appMiniWindowRowTouchHelper;
    }

    public void handleMessage(@NotNull Message message) {
        Intrinsics.checkParameterIsNotNull(message, "msg");
        super.handleMessage(message);
        if (message.what == 1) {
            this.this$0.handleHideNotificationPanel();
        }
    }
}
