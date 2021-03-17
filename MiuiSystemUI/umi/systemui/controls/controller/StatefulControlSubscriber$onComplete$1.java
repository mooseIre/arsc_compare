package com.android.systemui.controls.controller;

import android.util.Log;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Lambda;

/* compiled from: StatefulControlSubscriber.kt */
final class StatefulControlSubscriber$onComplete$1 extends Lambda implements Function0<Unit> {
    final /* synthetic */ StatefulControlSubscriber this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    StatefulControlSubscriber$onComplete$1(StatefulControlSubscriber statefulControlSubscriber) {
        super(0);
        this.this$0 = statefulControlSubscriber;
    }

    public final void invoke() {
        if (this.this$0.subscriptionOpen) {
            this.this$0.subscriptionOpen = false;
            Log.i("StatefulControlSubscriber", "onComplete receive from '" + this.this$0.provider.getComponentName() + '\'');
        }
    }
}
