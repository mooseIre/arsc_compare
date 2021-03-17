package com.android.systemui.controls.controller;

import android.content.ComponentName;
import android.service.controls.Control;
import com.android.systemui.controls.controller.ControlsBindingController;
import java.util.List;
import java.util.function.Consumer;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlsControllerImpl.kt */
public final class ControlsControllerImpl$loadForComponent$2 implements ControlsBindingController.LoadCallback {
    final /* synthetic */ ComponentName $componentName;
    final /* synthetic */ Consumer $dataCallback;
    final /* synthetic */ ControlsControllerImpl this$0;

    ControlsControllerImpl$loadForComponent$2(ControlsControllerImpl controlsControllerImpl, ComponentName componentName, Consumer consumer) {
        this.this$0 = controlsControllerImpl;
        this.$componentName = componentName;
        this.$dataCallback = consumer;
    }

    public void accept(@NotNull List<Control> list) {
        Intrinsics.checkParameterIsNotNull(list, "controls");
        this.this$0.executor.execute(new ControlsControllerImpl$loadForComponent$2$accept$1(this, list));
    }

    public void error(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "message");
        this.this$0.executor.execute(new ControlsControllerImpl$loadForComponent$2$error$1(this));
    }
}
