package com.android.systemui.controls.ui;

import java.util.List;

/* compiled from: ControlsUiControllerImpl.kt */
final class ControlsUiControllerImpl$createCallback$1$onServicesUpdated$1 implements Runnable {
    final /* synthetic */ List $lastItems;
    final /* synthetic */ ControlsUiControllerImpl$createCallback$1 this$0;

    ControlsUiControllerImpl$createCallback$1$onServicesUpdated$1(ControlsUiControllerImpl$createCallback$1 controlsUiControllerImpl$createCallback$1, List list) {
        this.this$0 = controlsUiControllerImpl$createCallback$1;
        this.$lastItems = list;
    }

    public final void run() {
        ControlsUiControllerImpl.access$getParent$p(this.this$0.this$0).removeAllViews();
        if (this.$lastItems.size() > 0) {
            this.this$0.$onResult.invoke(this.$lastItems);
        }
    }
}
