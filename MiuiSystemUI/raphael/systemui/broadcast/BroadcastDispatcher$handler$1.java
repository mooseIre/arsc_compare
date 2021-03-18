package com.android.systemui.broadcast;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: BroadcastDispatcher.kt */
public final class BroadcastDispatcher$handler$1 extends Handler {
    private int currentUser;
    final /* synthetic */ BroadcastDispatcher this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    BroadcastDispatcher$handler$1(BroadcastDispatcher broadcastDispatcher, Looper looper) {
        super(looper);
        this.this$0 = broadcastDispatcher;
    }

    public final int getCurrentUser() {
        return this.currentUser;
    }

    public void handleMessage(@NotNull Message message) {
        int i;
        Intrinsics.checkParameterIsNotNull(message, "msg");
        int i2 = message.what;
        if (i2 == 0) {
            Object obj = message.obj;
            if (obj != null) {
                ReceiverData receiverData = (ReceiverData) obj;
                if (receiverData.getUser().getIdentifier() == -2) {
                    i = this.currentUser;
                } else {
                    i = receiverData.getUser().getIdentifier();
                }
                if (i >= -1) {
                    UserBroadcastDispatcher userBroadcastDispatcher = (UserBroadcastDispatcher) this.this$0.receiversByUser.get(i, this.this$0.createUBRForUser(i));
                    this.this$0.receiversByUser.put(i, userBroadcastDispatcher);
                    userBroadcastDispatcher.registerReceiver(receiverData);
                    return;
                }
                throw new IllegalStateException("Attempting to register receiver for invalid user {" + i + '}');
            }
            throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.broadcast.ReceiverData");
        } else if (i2 == 1) {
            int size = this.this$0.receiversByUser.size();
            for (int i3 = 0; i3 < size; i3++) {
                UserBroadcastDispatcher userBroadcastDispatcher2 = (UserBroadcastDispatcher) this.this$0.receiversByUser.valueAt(i3);
                Object obj2 = message.obj;
                if (obj2 != null) {
                    userBroadcastDispatcher2.unregisterReceiver((BroadcastReceiver) obj2);
                } else {
                    throw new TypeCastException("null cannot be cast to non-null type android.content.BroadcastReceiver");
                }
            }
        } else if (i2 == 2) {
            UserBroadcastDispatcher userBroadcastDispatcher3 = (UserBroadcastDispatcher) this.this$0.receiversByUser.get(message.arg1);
            if (userBroadcastDispatcher3 != null) {
                Object obj3 = message.obj;
                if (obj3 != null) {
                    userBroadcastDispatcher3.unregisterReceiver((BroadcastReceiver) obj3);
                    return;
                }
                throw new TypeCastException("null cannot be cast to non-null type android.content.BroadcastReceiver");
            }
        } else if (i2 == 3) {
            this.currentUser = message.arg1;
        } else if (i2 != 99) {
            super.handleMessage(message);
        } else {
            this.currentUser = ActivityManager.getCurrentUser();
        }
    }
}
