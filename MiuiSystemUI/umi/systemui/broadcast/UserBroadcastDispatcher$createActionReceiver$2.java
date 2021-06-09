package com.android.systemui.broadcast;

import android.content.BroadcastReceiver;
import android.util.Log;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* compiled from: UserBroadcastDispatcher.kt */
final class UserBroadcastDispatcher$createActionReceiver$2 extends Lambda implements Function1<BroadcastReceiver, Unit> {
    final /* synthetic */ String $action;
    final /* synthetic */ UserBroadcastDispatcher this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    UserBroadcastDispatcher$createActionReceiver$2(UserBroadcastDispatcher userBroadcastDispatcher, String str) {
        super(1);
        this.this$0 = userBroadcastDispatcher;
        this.$action = str;
    }

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Unit invoke(BroadcastReceiver broadcastReceiver) {
        invoke(broadcastReceiver);
        return Unit.INSTANCE;
    }

    public final void invoke(@NotNull BroadcastReceiver broadcastReceiver) {
        Intrinsics.checkParameterIsNotNull(broadcastReceiver, "$receiver");
        try {
            UserBroadcastDispatcher.access$getContext$p(this.this$0).unregisterReceiver(broadcastReceiver);
            UserBroadcastDispatcher.access$getLogger$p(this.this$0).logContextReceiverUnregistered(UserBroadcastDispatcher.access$getUserId$p(this.this$0), this.$action);
        } catch (IllegalArgumentException e) {
            Log.e("UserBroadcastDispatcher", "Trying to unregister unregistered receiver for user " + UserBroadcastDispatcher.access$getUserId$p(this.this$0) + ", " + "action " + this.$action, new IllegalStateException(e));
        }
    }
}
