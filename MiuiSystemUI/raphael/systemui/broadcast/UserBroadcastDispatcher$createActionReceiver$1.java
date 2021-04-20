package com.android.systemui.broadcast;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.UserHandle;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: UserBroadcastDispatcher.kt */
public final class UserBroadcastDispatcher$createActionReceiver$1 extends Lambda implements Function2<BroadcastReceiver, IntentFilter, Unit> {
    final /* synthetic */ UserBroadcastDispatcher this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    UserBroadcastDispatcher$createActionReceiver$1(UserBroadcastDispatcher userBroadcastDispatcher) {
        super(2);
        this.this$0 = userBroadcastDispatcher;
    }

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object, java.lang.Object] */
    @Override // kotlin.jvm.functions.Function2
    public /* bridge */ /* synthetic */ Unit invoke(BroadcastReceiver broadcastReceiver, IntentFilter intentFilter) {
        invoke(broadcastReceiver, intentFilter);
        return Unit.INSTANCE;
    }

    public final void invoke(@NotNull BroadcastReceiver broadcastReceiver, @NotNull IntentFilter intentFilter) {
        Intrinsics.checkParameterIsNotNull(broadcastReceiver, "$receiver");
        Intrinsics.checkParameterIsNotNull(intentFilter, "it");
        this.this$0.context.registerReceiverAsUser(broadcastReceiver, UserHandle.of(this.this$0.userId), intentFilter, null, this.this$0.bgHandler);
        this.this$0.logger.logContextReceiverRegistered(this.this$0.userId, intentFilter);
    }
}
