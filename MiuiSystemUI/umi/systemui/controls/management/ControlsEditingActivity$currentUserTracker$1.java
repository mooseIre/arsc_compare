package com.android.systemui.controls.management;

import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.settings.CurrentUserTracker;

/* compiled from: ControlsEditingActivity.kt */
public final class ControlsEditingActivity$currentUserTracker$1 extends CurrentUserTracker {
    private final int startingUser;
    final /* synthetic */ ControlsEditingActivity this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    ControlsEditingActivity$currentUserTracker$1(ControlsEditingActivity controlsEditingActivity, BroadcastDispatcher broadcastDispatcher, BroadcastDispatcher broadcastDispatcher2) {
        super(broadcastDispatcher2);
        this.this$0 = controlsEditingActivity;
        this.startingUser = ControlsEditingActivity.access$getController$p(controlsEditingActivity).getCurrentUserId();
    }

    @Override // com.android.systemui.settings.CurrentUserTracker
    public void onUserSwitched(int i) {
        if (i != this.startingUser) {
            stopTracking();
            this.this$0.finish();
        }
    }
}
