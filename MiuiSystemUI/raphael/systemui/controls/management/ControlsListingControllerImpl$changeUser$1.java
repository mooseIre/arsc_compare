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
        if (this.this$0.userChangeInProgress.decrementAndGet() == 0) {
            this.this$0.currentUserId = this.$newUser.getIdentifier();
            Context createContextAsUser = this.this$0.context.createContextAsUser(this.$newUser, 0);
            ControlsListingControllerImpl controlsListingControllerImpl = this.this$0;
            Function1 function1 = controlsListingControllerImpl.serviceListingBuilder;
            Intrinsics.checkExpressionValueIsNotNull(createContextAsUser, "contextForUser");
            controlsListingControllerImpl.serviceListing = (ServiceListing) function1.invoke(createContextAsUser);
            this.this$0.serviceListing.addCallback(this.this$0.serviceListingCallback);
            this.this$0.serviceListing.setListening(true);
            this.this$0.serviceListing.reload();
        }
    }
}
