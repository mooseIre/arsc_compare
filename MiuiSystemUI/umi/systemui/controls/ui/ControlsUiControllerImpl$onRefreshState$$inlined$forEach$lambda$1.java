package com.android.systemui.controls.ui;

import android.content.ComponentName;
import android.service.controls.Control;

/* compiled from: ControlsUiControllerImpl.kt */
final class ControlsUiControllerImpl$onRefreshState$$inlined$forEach$lambda$1 implements Runnable {
    final /* synthetic */ ControlWithState $cws;
    final /* synthetic */ ControlKey $key;
    final /* synthetic */ ControlsUiControllerImpl this$0;

    ControlsUiControllerImpl$onRefreshState$$inlined$forEach$lambda$1(ControlKey controlKey, ControlWithState controlWithState, Control control, ControlsUiControllerImpl controlsUiControllerImpl, ComponentName componentName) {
        this.$key = controlKey;
        this.$cws = controlWithState;
        this.this$0 = controlsUiControllerImpl;
    }

    public final void run() {
        ControlViewHolder controlViewHolder = (ControlViewHolder) this.this$0.controlViewsById.get(this.$key);
        if (controlViewHolder != null) {
            controlViewHolder.bindData(this.$cws);
        }
    }
}
