package com.android.systemui.broadcast;

import android.content.BroadcastReceiver;
import com.android.systemui.broadcast.logging.BroadcastDispatcherLogger;

/* compiled from: ActionReceiver.kt */
final class ActionReceiver$onReceive$1$$special$$inlined$forEach$lambda$1 implements Runnable {
    final /* synthetic */ ReceiverData $it;
    final /* synthetic */ ActionReceiver$onReceive$1 this$0;

    ActionReceiver$onReceive$1$$special$$inlined$forEach$lambda$1(ReceiverData receiverData, ActionReceiver$onReceive$1 actionReceiver$onReceive$1) {
        this.$it = receiverData;
        this.this$0 = actionReceiver$onReceive$1;
    }

    public final void run() {
        this.$it.getReceiver().setPendingResult(this.this$0.this$0.getPendingResult());
        BroadcastReceiver receiver = this.$it.getReceiver();
        ActionReceiver$onReceive$1 actionReceiver$onReceive$1 = this.this$0;
        receiver.onReceive(actionReceiver$onReceive$1.$context, actionReceiver$onReceive$1.$intent);
        BroadcastDispatcherLogger broadcastDispatcherLogger = this.this$0.this$0.logger;
        ActionReceiver$onReceive$1 actionReceiver$onReceive$12 = this.this$0;
        broadcastDispatcherLogger.logBroadcastDispatched(actionReceiver$onReceive$12.$id, actionReceiver$onReceive$12.this$0.action, this.$it.getReceiver());
    }
}
