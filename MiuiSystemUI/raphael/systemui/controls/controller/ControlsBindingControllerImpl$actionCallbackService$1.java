package com.android.systemui.controls.controller;

import android.os.IBinder;
import android.service.controls.IControlsActionCallback;
import com.android.systemui.controls.controller.ControlsBindingControllerImpl;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlsBindingControllerImpl.kt */
public final class ControlsBindingControllerImpl$actionCallbackService$1 extends IControlsActionCallback.Stub {
    final /* synthetic */ ControlsBindingControllerImpl this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    ControlsBindingControllerImpl$actionCallbackService$1(ControlsBindingControllerImpl controlsBindingControllerImpl) {
        this.this$0 = controlsBindingControllerImpl;
    }

    public void accept(@NotNull IBinder iBinder, @NotNull String str, int i) {
        Intrinsics.checkParameterIsNotNull(iBinder, "token");
        Intrinsics.checkParameterIsNotNull(str, "controlId");
        this.this$0.backgroundExecutor.execute(new ControlsBindingControllerImpl.OnActionResponseRunnable(this.this$0, iBinder, str, i));
    }
}
