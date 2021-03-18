package com.android.systemui.broadcast;

import android.content.BroadcastReceiver;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;

/* compiled from: ActionReceiver.kt */
final class ActionReceiver$removeReceiver$1 extends Lambda implements Function1<ReceiverData, Boolean> {
    final /* synthetic */ BroadcastReceiver $receiver;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    ActionReceiver$removeReceiver$1(BroadcastReceiver broadcastReceiver) {
        super(1);
        this.$receiver = broadcastReceiver;
    }

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Boolean invoke(ReceiverData receiverData) {
        return Boolean.valueOf(invoke(receiverData));
    }

    public final boolean invoke(ReceiverData receiverData) {
        return Intrinsics.areEqual(receiverData.getReceiver(), this.$receiver);
    }
}
