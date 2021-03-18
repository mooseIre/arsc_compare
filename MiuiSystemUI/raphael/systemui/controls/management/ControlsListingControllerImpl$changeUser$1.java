package com.android.systemui.controls.management;

import android.content.Context;
import android.os.UserHandle;
import com.android.settingslib.applications.ServiceListing;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: ControlsListingControllerImpl.kt */
final class ControlsListingControllerImpl$changeUser$1 implements Runnable {
    final /* synthetic */ UserHandle $newUser;
    final /* synthetic */ ControlsListingControllerImpl this$0;

    ControlsListingControllerImpl$changeUser$1(ControlsListingControllerImpl controlsListingControllerImpl, UserHandle userHandle) {
        this.this$0 = controlsListingControllerImpl;
        this.$newUser = userHandle;
    }

    public final void run() {
        if (ControlsListingControllerImpl.access$getUserChangeInProgress$p(this.this$0).decrementAndGet() == 0) {
            ControlsListingControllerImpl.access$setCurrentUserId$p(this.this$0, this.$newUser.getIdentifier());
            Context createContextAsUser = ControlsListingControllerImpl.access$getContext$p(this.this$0).createContextAsUser(this.$newUser, 0);
            ControlsListingControllerImpl controlsListingControllerImpl = this.this$0;
            Function1 access$getServiceListingBuilder$p = ControlsListingControllerImpl.access$getServiceListingBuilder$p(controlsListingControllerImpl);
            Intrinsics.checkExpressionValueIsNotNull(createContextAsUser, "contextForUser");
            ControlsListingControllerImpl.access$setServiceListing$p(controlsListingControllerImpl, (ServiceListing) access$getServiceListingBuilder$p.invoke(createContextAsUser));
            ControlsListingControllerImpl.access$getServiceListing$p(this.this$0).addCallback(ControlsListingControllerImpl.access$getServiceListingCallback$p(this.this$0));
            ControlsListingControllerImpl.access$getServiceListing$p(this.this$0).setListening(true);
            ControlsListingControllerImpl.access$getServiceListing$p(this.this$0).reload();
        }
    }
}
