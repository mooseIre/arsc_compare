package com.android.systemui.controlcenter.phone;

import com.android.systemui.controlcenter.phone.widget.MiuiQSPanel$MiuiRecord;
import com.android.systemui.plugins.miui.controls.MiPlayEntranceViewCallback;

/* compiled from: ControlCenterPanelView.kt */
public final class ControlCenterPanelView$addControlsPlugin$$inlined$let$lambda$1 implements MiPlayEntranceViewCallback {
    final /* synthetic */ MiuiQSPanel$MiuiRecord $miPlayRecord;
    final /* synthetic */ ControlCenterPanelView this$0;

    @Override // com.android.systemui.plugins.miui.controls.MiPlayEntranceViewCallback
    public void hideEntranceView() {
    }

    ControlCenterPanelView$addControlsPlugin$$inlined$let$lambda$1(MiuiQSPanel$MiuiRecord miuiQSPanel$MiuiRecord, ControlCenterPanelView controlCenterPanelView) {
        this.$miPlayRecord = miuiQSPanel$MiuiRecord;
        this.this$0 = controlCenterPanelView;
    }

    @Override // com.android.systemui.plugins.miui.controls.MiPlayEntranceViewCallback
    public void showEntranceView() {
        this.this$0.showDetail(true, this.$miPlayRecord);
    }
}
