package com.android.systemui.broadcast;

import android.content.BroadcastReceiver;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: UserBroadcastDispatcher.kt */
public final class UserBroadcastDispatcher$bgHandler$1 extends Handler {
    final /* synthetic */ UserBroadcastDispatcher this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    UserBroadcastDispatcher$bgHandler$1(UserBroadcastDispatcher userBroadcastDispatcher, Looper looper) {
        super(looper);
        this.this$0 = userBroadcastDispatcher;
    }

    public void handleMessage(@NotNull Message message) {
        Intrinsics.checkParameterIsNotNull(message, "msg");
        int i = message.what;
        if (i == 0) {
            UserBroadcastDispatcher userBroadcastDispatcher = this.this$0;
            Object obj = message.obj;
            if (obj != null) {
                userBroadcastDispatcher.handleRegisterReceiver((ReceiverData) obj);
                return;
            }
            throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.broadcast.ReceiverData");
        } else if (i == 1) {
            UserBroadcastDispatcher userBroadcastDispatcher2 = this.this$0;
            Object obj2 = message.obj;
            if (obj2 != null) {
                userBroadcastDispatcher2.handleUnregisterReceiver((BroadcastReceiver) obj2);
                return;
            }
            throw new TypeCastException("null cannot be cast to non-null type android.content.BroadcastReceiver");
        }
    }
}
