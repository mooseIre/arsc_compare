package com.android.systemui.controls.controller;

import com.android.systemui.controls.ControlsServiceInfo;
import com.android.systemui.controls.management.ControlsListingController;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlsControllerImpl.kt */
public final class ControlsControllerImpl$listingCallback$1 implements ControlsListingController.ControlsListingCallback {
    final /* synthetic */ ControlsControllerImpl this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    ControlsControllerImpl$listingCallback$1(ControlsControllerImpl controlsControllerImpl) {
        this.this$0 = controlsControllerImpl;
    }

    @Override // com.android.systemui.controls.management.ControlsListingController.ControlsListingCallback
    public void onServicesUpdated(@NotNull List<ControlsServiceInfo> list) {
        Intrinsics.checkParameterIsNotNull(list, "serviceInfos");
        ControlsControllerImpl.access$getExecutor$p(this.this$0).execute(new ControlsControllerImpl$listingCallback$1$onServicesUpdated$1(this, list));
    }
}
