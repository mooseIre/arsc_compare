package com.android.systemui.controls.management;

import com.android.systemui.controls.management.ControlsModel;

/* compiled from: ControlsFavoritingActivity.kt */
public final class ControlsFavoritingActivity$controlsModelCallback$1 implements ControlsModel.ControlsModelCallback {
    final /* synthetic */ ControlsFavoritingActivity this$0;

    ControlsFavoritingActivity$controlsModelCallback$1(ControlsFavoritingActivity controlsFavoritingActivity) {
        this.this$0 = controlsFavoritingActivity;
    }

    public void onFirstChange() {
        ControlsFavoritingActivity.access$getDoneButton$p(this.this$0).setEnabled(true);
    }
}
