package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiHeadsUpPolicy.kt */
public final class MiuiHeadsUpPolicy$mCloseSystemDialogReceiver$1 extends BroadcastReceiver {
    final /* synthetic */ MiuiHeadsUpPolicy this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    MiuiHeadsUpPolicy$mCloseSystemDialogReceiver$1(MiuiHeadsUpPolicy miuiHeadsUpPolicy) {
        this.this$0 = miuiHeadsUpPolicy;
    }

    public void onReceive(@Nullable Context context, @Nullable Intent intent) {
        this.this$0.releaseHeadsUps();
    }
}
