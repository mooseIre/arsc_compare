package com.android.systemui.controls.controller;

import android.service.controls.IControlsSubscription;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Lambda;

/* compiled from: StatefulControlSubscriber.kt */
final class StatefulControlSubscriber$onSubscribe$1 extends Lambda implements Function0<Unit> {
    final /* synthetic */ IControlsSubscription $subs;
    final /* synthetic */ StatefulControlSubscriber this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    StatefulControlSubscriber$onSubscribe$1(StatefulControlSubscriber statefulControlSubscriber, IControlsSubscription iControlsSubscription) {
        super(0);
        this.this$0 = statefulControlSubscriber;
        this.$subs = iControlsSubscription;
    }

    public final void invoke() {
        this.this$0.subscriptionOpen = true;
        this.this$0.subscription = this.$subs;
        this.this$0.provider.startSubscription(this.$subs, this.this$0.requestLimit);
    }
}
