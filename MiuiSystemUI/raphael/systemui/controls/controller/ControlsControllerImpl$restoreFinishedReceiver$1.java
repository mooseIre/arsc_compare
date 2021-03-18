package com.android.systemui.controls.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlsControllerImpl.kt */
public final class ControlsControllerImpl$restoreFinishedReceiver$1 extends BroadcastReceiver {
    final /* synthetic */ ControlsControllerImpl this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    ControlsControllerImpl$restoreFinishedReceiver$1(ControlsControllerImpl controlsControllerImpl) {
        this.this$0 = controlsControllerImpl;
    }

    public void onReceive(@NotNull Context context, @NotNull Intent intent) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(intent, "intent");
        if (intent.getIntExtra("android.intent.extra.USER_ID", -10000) == this.this$0.getCurrentUserId()) {
            ControlsControllerImpl.access$getExecutor$p(this.this$0).execute(new ControlsControllerImpl$restoreFinishedReceiver$1$onReceive$1(this));
        }
    }
}
