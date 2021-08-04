package com.android.systemui.controls.management;

import android.util.Log;
import com.android.systemui.controls.ControlsServiceInfo;
import com.android.systemui.controls.management.ControlsListingController;
import java.util.List;

/* access modifiers changed from: package-private */
/* compiled from: ControlsListingControllerImpl.kt */
public final class ControlsListingControllerImpl$addCallback$1 implements Runnable {
    final /* synthetic */ ControlsListingController.ControlsListingCallback $listener;
    final /* synthetic */ ControlsListingControllerImpl this$0;

    ControlsListingControllerImpl$addCallback$1(ControlsListingControllerImpl controlsListingControllerImpl, ControlsListingController.ControlsListingCallback controlsListingCallback) {
        this.this$0 = controlsListingControllerImpl;
        this.$listener = controlsListingCallback;
    }

    public final void run() {
        if (ControlsListingControllerImpl.access$getUserChangeInProgress$p(this.this$0).get() > 0) {
            this.this$0.addCallback(this.$listener);
            return;
        }
        List<ControlsServiceInfo> currentServices = this.this$0.getCurrentServices();
        Log.d("ControlsListingControllerImpl", "Subscribing callback, service count: " + currentServices.size());
        ControlsListingControllerImpl.access$getCallbacks$p(this.this$0).add(this.$listener);
        this.$listener.onServicesUpdated(currentServices);
    }
}
