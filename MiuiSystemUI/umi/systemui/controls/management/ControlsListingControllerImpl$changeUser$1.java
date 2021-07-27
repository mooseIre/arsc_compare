package com.android.systemui.controls.management;

import android.os.UserHandle;

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
            ControlsListingControllerImpl.access$getContext$p(this.this$0).createContextAsUser(this.$newUser, 0);
        }
    }
}
