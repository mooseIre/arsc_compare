package com.android.systemui.controls.management;

import android.view.View;

/* access modifiers changed from: package-private */
/* compiled from: ControlsEditingActivity.kt */
public final class ControlsEditingActivity$bindButtons$$inlined$apply$lambda$1 implements View.OnClickListener {
    final /* synthetic */ ControlsEditingActivity this$0;

    ControlsEditingActivity$bindButtons$$inlined$apply$lambda$1(ControlsEditingActivity controlsEditingActivity) {
        this.this$0 = controlsEditingActivity;
    }

    public final void onClick(View view) {
        this.this$0.saveFavorites();
        this.this$0.animateExitAndFinish();
        this.this$0.globalActionsComponent.handleShowGlobalActionsMenu();
    }
}
