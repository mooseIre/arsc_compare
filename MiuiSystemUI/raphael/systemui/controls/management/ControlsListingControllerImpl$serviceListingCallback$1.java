package com.android.systemui.controls.management;

import android.content.ComponentName;
import android.content.pm.ServiceInfo;
import android.util.Log;
import com.android.settingslib.applications.ServiceListing;
import com.android.systemui.controls.ControlsServiceInfo;
import com.android.systemui.controls.management.ControlsListingController;
import java.util.LinkedHashSet;
import java.util.List;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: ControlsListingControllerImpl.kt */
final class ControlsListingControllerImpl$serviceListingCallback$1 implements ServiceListing.Callback {
    final /* synthetic */ ControlsListingControllerImpl this$0;

    ControlsListingControllerImpl$serviceListingCallback$1(ControlsListingControllerImpl controlsListingControllerImpl) {
        this.this$0 = controlsListingControllerImpl;
    }

    @Override // com.android.settingslib.applications.ServiceListing.Callback
    public final void onServicesReloaded(List<ServiceInfo> list) {
        Intrinsics.checkExpressionValueIsNotNull(list, "it");
        final List<ServiceInfo> list2 = CollectionsKt___CollectionsKt.toList(list);
        final LinkedHashSet linkedHashSet = new LinkedHashSet();
        for (ServiceInfo serviceInfo : list2) {
            ComponentName componentName = serviceInfo.getComponentName();
            Intrinsics.checkExpressionValueIsNotNull(componentName, "s.getComponentName()");
            linkedHashSet.add(componentName);
        }
        ControlsListingControllerImpl.access$getBackgroundExecutor$p(this.this$0).execute(new Runnable(this) {
            /* class com.android.systemui.controls.management.ControlsListingControllerImpl$serviceListingCallback$1.AnonymousClass1 */
            final /* synthetic */ ControlsListingControllerImpl$serviceListingCallback$1 this$0;

            {
                this.this$0 = r1;
            }

            public final void run() {
                if (ControlsListingControllerImpl.access$getUserChangeInProgress$p(this.this$0.this$0).get() <= 0 && !linkedHashSet.equals(ControlsListingControllerImpl.access$getAvailableComponents$p(this.this$0.this$0))) {
                    Log.d("ControlsListingControllerImpl", "ServiceConfig reloaded, count: " + linkedHashSet.size());
                    ControlsListingControllerImpl.access$setAvailableComponents$p(this.this$0.this$0, linkedHashSet);
                    ControlsListingControllerImpl.access$setAvailableServices$p(this.this$0.this$0, list2);
                    List<ControlsServiceInfo> currentServices = this.this$0.this$0.getCurrentServices();
                    for (ControlsListingController.ControlsListingCallback controlsListingCallback : ControlsListingControllerImpl.access$getCallbacks$p(this.this$0.this$0)) {
                        controlsListingCallback.onServicesUpdated(currentServices);
                    }
                }
            }
        });
    }
}
