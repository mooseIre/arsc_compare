package com.android.systemui.controls.management;

import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.settings.CurrentUserTracker;

/* compiled from: ControlsRequestDialog.kt */
public final class ControlsRequestDialog$currentUserTracker$1 extends CurrentUserTracker {
    private final int startingUser;
    final /* synthetic */ ControlsRequestDialog this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    ControlsRequestDialog$currentUserTracker$1(ControlsRequestDialog controlsRequestDialog, BroadcastDispatcher broadcastDispatcher) {
        super(broadcastDispatcher);
        this.this$0 = controlsRequestDialog;
        this.startingUser = controlsRequestDialog.controller.getCurrentUserId();
    }

    public void onUserSwitched(int i) {
        if (i != this.startingUser) {
            stopTracking();
            this.this$0.finish();
        }
    }
}
