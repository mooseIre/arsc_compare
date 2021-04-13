package com.android.systemui.controls.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlsControllerImpl.kt */
public final class ControlsControllerImpl$userSwitchReceiver$1 extends BroadcastReceiver {
    final /* synthetic */ ControlsControllerImpl this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    ControlsControllerImpl$userSwitchReceiver$1(ControlsControllerImpl controlsControllerImpl) {
        this.this$0 = controlsControllerImpl;
    }

    public void onReceive(@NotNull Context context, @NotNull Intent intent) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(intent, "intent");
        if (Intrinsics.areEqual(intent.getAction(), "android.intent.action.USER_SWITCHED")) {
            ControlsControllerImpl.access$setUserChanging$p(this.this$0, true);
            UserHandle of = UserHandle.of(intent.getIntExtra("android.intent.extra.user_handle", getSendingUserId()));
            if (Intrinsics.areEqual(ControlsControllerImpl.access$getCurrentUser$p(this.this$0), of)) {
                ControlsControllerImpl.access$setUserChanging$p(this.this$0, false);
                return;
            }
            ControlsControllerImpl controlsControllerImpl = this.this$0;
            Intrinsics.checkExpressionValueIsNotNull(of, "newUser");
            ControlsControllerImpl.access$setValuesForUser(controlsControllerImpl, of);
        }
    }
}
