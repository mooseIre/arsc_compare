package com.android.systemui.controls.controller;

import android.service.controls.IControlsSubscription;

/* compiled from: StatefulControlSubscriber.kt */
final class StatefulControlSubscriber$cancel$1 implements Runnable {
    final /* synthetic */ StatefulControlSubscriber this$0;

    StatefulControlSubscriber$cancel$1(StatefulControlSubscriber statefulControlSubscriber) {
        this.this$0 = statefulControlSubscriber;
    }

    public final void run() {
        if (this.this$0.subscriptionOpen) {
            this.this$0.subscriptionOpen = false;
            IControlsSubscription access$getSubscription$p = this.this$0.subscription;
            if (access$getSubscription$p != null) {
                this.this$0.provider.cancelSubscription(access$getSubscription$p);
            }
            this.this$0.subscription = null;
        }
    }
}
