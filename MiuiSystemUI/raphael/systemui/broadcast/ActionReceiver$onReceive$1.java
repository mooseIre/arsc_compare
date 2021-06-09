package com.android.systemui.broadcast;

import android.content.Context;
import android.content.Intent;

/* compiled from: ActionReceiver.kt */
final class ActionReceiver$onReceive$1 implements Runnable {
    final /* synthetic */ Context $context;
    final /* synthetic */ int $id;
    final /* synthetic */ Intent $intent;
    final /* synthetic */ ActionReceiver this$0;

    ActionReceiver$onReceive$1(ActionReceiver actionReceiver, Intent intent, Context context, int i) {
        this.this$0 = actionReceiver;
        this.$intent = intent;
        this.$context = context;
        this.$id = i;
    }

    public final void run() {
        for (ReceiverData receiverData : ActionReceiver.access$getReceiverDatas$p(this.this$0)) {
            if (receiverData.getFilter().matchCategories(this.$intent.getCategories()) == null) {
                receiverData.getExecutor().execute(new ActionReceiver$onReceive$1$$special$$inlined$forEach$lambda$1(receiverData, this));
            }
        }
    }
}
