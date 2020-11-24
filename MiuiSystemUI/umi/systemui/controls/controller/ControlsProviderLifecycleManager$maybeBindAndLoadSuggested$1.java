package com.android.systemui.controls.controller;

import android.service.controls.IControlsSubscriber;
import android.util.Log;

/* compiled from: ControlsProviderLifecycleManager.kt */
final class ControlsProviderLifecycleManager$maybeBindAndLoadSuggested$1 implements Runnable {
    final /* synthetic */ IControlsSubscriber.Stub $subscriber;
    final /* synthetic */ ControlsProviderLifecycleManager this$0;

    ControlsProviderLifecycleManager$maybeBindAndLoadSuggested$1(ControlsProviderLifecycleManager controlsProviderLifecycleManager, IControlsSubscriber.Stub stub) {
        this.this$0 = controlsProviderLifecycleManager;
        this.$subscriber = stub;
    }

    public final void run() {
        String access$getTAG$p = this.this$0.TAG;
        Log.d(access$getTAG$p, "Timeout waiting onLoadSuggested for " + this.this$0.getComponentName());
        this.$subscriber.onError(this.this$0.getToken(), "Timeout waiting onLoadSuggested");
        this.this$0.unbindService();
    }
}
