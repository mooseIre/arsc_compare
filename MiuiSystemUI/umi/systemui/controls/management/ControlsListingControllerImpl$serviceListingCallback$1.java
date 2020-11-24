package com.android.systemui.controls.management;

import android.content.ComponentName;
import android.content.pm.ServiceInfo;
import android.util.Log;
import com.android.settingslib.applications.ServiceListing;
import com.android.systemui.controls.ControlsServiceInfo;
import com.android.systemui.controls.management.ControlsListingController;
import java.util.LinkedHashSet;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: ControlsListingControllerImpl.kt */
final class ControlsListingControllerImpl$serviceListingCallback$1 implements ServiceListing.Callback {
    final /* synthetic */ ControlsListingControllerImpl this$0;

    ControlsListingControllerImpl$serviceListingCallback$1(ControlsListingControllerImpl controlsListingControllerImpl) {
        this.this$0 = controlsListingControllerImpl;
    }

    public final void onServicesReloaded(List<ServiceInfo> list) {
        Intrinsics.checkExpressionValueIsNotNull(list, "it");
        final List<T> list2 = CollectionsKt___CollectionsKt.toList(list);
        final LinkedHashSet linkedHashSet = new LinkedHashSet();
        for (T componentName : list2) {
            ComponentName componentName2 = componentName.getComponentName();
            Intrinsics.checkExpressionValueIsNotNull(componentName2, "s.getComponentName()");
            linkedHashSet.add(componentName2);
        }
        this.this$0.backgroundExecutor.execute(new Runnable(this) {
            final /* synthetic */ ControlsListingControllerImpl$serviceListingCallback$1 this$0;

            {
                this.this$0 = r1;
            }

            public final void run() {
                if (this.this$0.this$0.userChangeInProgress.get() <= 0 && !linkedHashSet.equals(this.this$0.this$0.availableComponents)) {
                    Log.d("ControlsListingControllerImpl", "ServiceConfig reloaded, count: " + linkedHashSet.size());
                    this.this$0.this$0.availableComponents = linkedHashSet;
                    this.this$0.this$0.availableServices = list2;
                    List<ControlsServiceInfo> currentServices = this.this$0.this$0.getCurrentServices();
                    for (ControlsListingController.ControlsListingCallback onServicesUpdated : this.this$0.this$0.callbacks) {
                        onServicesUpdated.onServicesUpdated(currentServices);
                    }
                }
            }
        });
    }
}
