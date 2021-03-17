package com.android.systemui.controls.management;

import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.settings.CurrentUserTracker;

/* compiled from: ControlsProviderSelectorActivity.kt */
public final class ControlsProviderSelectorActivity$currentUserTracker$1 extends CurrentUserTracker {
    private final int startingUser;
    final /* synthetic */ ControlsProviderSelectorActivity this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    ControlsProviderSelectorActivity$currentUserTracker$1(ControlsProviderSelectorActivity controlsProviderSelectorActivity, BroadcastDispatcher broadcastDispatcher, BroadcastDispatcher broadcastDispatcher2) {
        super(broadcastDispatcher2);
        this.this$0 = controlsProviderSelectorActivity;
        this.startingUser = controlsProviderSelectorActivity.listingController.getCurrentUserId();
    }

    @Override // com.android.systemui.settings.CurrentUserTracker
    public void onUserSwitched(int i) {
        if (i != this.startingUser) {
            stopTracking();
            this.this$0.finish();
        }
    }
}
