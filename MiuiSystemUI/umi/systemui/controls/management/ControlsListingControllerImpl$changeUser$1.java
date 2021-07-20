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
        if (this.this$0.userChangeInProgress.decrementAndGet() == 0) {
            this.this$0.currentUserId = this.$newUser.getIdentifier();
            this.this$0.context.createContextAsUser(this.$newUser, 0);
        }
    }
}
