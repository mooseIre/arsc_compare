package com.android.systemui.controls.management;

import com.android.systemui.controls.management.FavoritesModel;

/* compiled from: ControlsEditingActivity.kt */
public final class ControlsEditingActivity$favoritesModelCallback$1 implements FavoritesModel.FavoritesModelCallback {
    final /* synthetic */ ControlsEditingActivity this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    ControlsEditingActivity$favoritesModelCallback$1(ControlsEditingActivity controlsEditingActivity) {
        this.this$0 = controlsEditingActivity;
    }

    @Override // com.android.systemui.controls.management.FavoritesModel.FavoritesModelCallback
    public void onNoneChanged(boolean z) {
        if (z) {
            ControlsEditingActivity.access$getSubtitle$p(this.this$0).setText(ControlsEditingActivity.access$getEMPTY_TEXT_ID$cp());
        } else {
            ControlsEditingActivity.access$getSubtitle$p(this.this$0).setText(ControlsEditingActivity.access$getSUBTITLE_ID$cp());
        }
    }

    @Override // com.android.systemui.controls.management.ControlsModel.ControlsModelCallback
    public void onFirstChange() {
        ControlsEditingActivity.access$getSaveButton$p(this.this$0).setEnabled(true);
    }
}
