package com.android.systemui.controls.ui;

import android.content.Context;
import android.view.View;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: ControlsUiControllerImpl.kt */
public final class ControlsUiControllerImpl$showInitialSetupView$1 implements View.OnClickListener {
    final /* synthetic */ ControlsUiControllerImpl this$0;

    ControlsUiControllerImpl$showInitialSetupView$1(ControlsUiControllerImpl controlsUiControllerImpl) {
        this.this$0 = controlsUiControllerImpl;
    }

    public final void onClick(@NotNull View view) {
        Intrinsics.checkParameterIsNotNull(view, "v");
        ControlsUiControllerImpl controlsUiControllerImpl = this.this$0;
        Context context = view.getContext();
        Intrinsics.checkExpressionValueIsNotNull(context, "v.context");
        controlsUiControllerImpl.startProviderSelectorActivity(context);
    }
}
