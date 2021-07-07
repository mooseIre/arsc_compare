package com.android.systemui.controls.management;

import android.view.View;

/* compiled from: ControlsEditingActivity.kt */
final class ControlsEditingActivity$bindButtons$$inlined$apply$lambda$1 implements View.OnClickListener {
    final /* synthetic */ ControlsEditingActivity this$0;

    ControlsEditingActivity$bindButtons$$inlined$apply$lambda$1(ControlsEditingActivity controlsEditingActivity) {
        this.this$0 = controlsEditingActivity;
    }

    public final void onClick(View view) {
        ControlsEditingActivity.access$saveFavorites(this.this$0);
        ControlsEditingActivity.access$animateExitAndFinish(this.this$0);
        ControlsEditingActivity.access$getGlobalActionsComponent$p(this.this$0).handleShowGlobalActionsMenu();
    }
}
