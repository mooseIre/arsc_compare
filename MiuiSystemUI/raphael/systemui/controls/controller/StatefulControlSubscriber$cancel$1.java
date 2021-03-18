package com.android.systemui.controls.controller;

import android.service.controls.IControlsSubscription;

/* access modifiers changed from: package-private */
/* compiled from: StatefulControlSubscriber.kt */
public final class StatefulControlSubscriber$cancel$1 implements Runnable {
    final /* synthetic */ StatefulControlSubscriber this$0;

    StatefulControlSubscriber$cancel$1(StatefulControlSubscriber statefulControlSubscriber) {
        this.this$0 = statefulControlSubscriber;
    }

    public final void run() {
        if (this.this$0.subscriptionOpen) {
            this.this$0.subscriptionOpen = false;
            IControlsSubscription iControlsSubscription = this.this$0.subscription;
            if (iControlsSubscription != null) {
                this.this$0.provider.cancelSubscription(iControlsSubscription);
            }
            this.this$0.subscription = null;
        }
    }
}
