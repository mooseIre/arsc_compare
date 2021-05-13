package com.android.systemui.controls.ui;

import android.content.Context;
import android.content.Intent;
import com.android.systemui.plugins.ActivityStarter;

/* compiled from: ControlsUiControllerImpl.kt */
final class ControlsUiControllerImpl$startActivity$1 implements ActivityStarter.OnDismissAction {
    final /* synthetic */ Context $context;
    final /* synthetic */ Intent $intent;
    final /* synthetic */ ControlsUiControllerImpl this$0;

    ControlsUiControllerImpl$startActivity$1(ControlsUiControllerImpl controlsUiControllerImpl, Context context, Intent intent) {
        this.this$0 = controlsUiControllerImpl;
        this.$context = context;
        this.$intent = intent;
    }

    @Override // com.android.systemui.plugins.ActivityStarter.OnDismissAction
    public final boolean onDismiss() {
        this.this$0.shadeController.collapsePanel(false);
        this.$context.startActivity(this.$intent);
        return true;
    }
}
