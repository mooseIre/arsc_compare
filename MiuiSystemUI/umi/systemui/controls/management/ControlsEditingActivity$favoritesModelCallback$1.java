package com.android.systemui.controls.management;

import com.android.systemui.controls.management.FavoritesModel;

/* compiled from: ControlsEditingActivity.kt */
public final class ControlsEditingActivity$favoritesModelCallback$1 implements FavoritesModel.FavoritesModelCallback {
    final /* synthetic */ ControlsEditingActivity this$0;

    ControlsEditingActivity$favoritesModelCallback$1(ControlsEditingActivity controlsEditingActivity) {
        this.this$0 = controlsEditingActivity;
    }

    public void onNoneChanged(boolean z) {
        if (z) {
            ControlsEditingActivity.access$getSubtitle$p(this.this$0).setText(ControlsEditingActivity.EMPTY_TEXT_ID);
        } else {
            ControlsEditingActivity.access$getSubtitle$p(this.this$0).setText(ControlsEditingActivity.SUBTITLE_ID);
        }
    }

    public void onFirstChange() {
        ControlsEditingActivity.access$getSaveButton$p(this.this$0).setEnabled(true);
    }
}
