package com.android.systemui.controls.controller;

import android.util.Log;

/* compiled from: ControlsControllerImpl.kt */
final class ControlsControllerImpl$restoreFinishedReceiver$1$onReceive$1 implements Runnable {
    final /* synthetic */ ControlsControllerImpl$restoreFinishedReceiver$1 this$0;

    ControlsControllerImpl$restoreFinishedReceiver$1$onReceive$1(ControlsControllerImpl$restoreFinishedReceiver$1 controlsControllerImpl$restoreFinishedReceiver$1) {
        this.this$0 = controlsControllerImpl$restoreFinishedReceiver$1;
    }

    public final void run() {
        Log.d("ControlsControllerImpl", "Restore finished, storing auxiliary favorites");
        this.this$0.this$0.getAuxiliaryPersistenceWrapper$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core().initialize();
        this.this$0.this$0.persistenceWrapper.storeFavorites(this.this$0.this$0.getAuxiliaryPersistenceWrapper$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core().getFavorites());
        ControlsControllerImpl controlsControllerImpl = this.this$0.this$0;
        controlsControllerImpl.resetFavorites(controlsControllerImpl.getAvailable());
    }
}
