package com.android.systemui.controls.controller;

import android.os.IBinder;
import android.service.controls.Control;
import android.util.Log;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Lambda;

/* compiled from: StatefulControlSubscriber.kt */
final class StatefulControlSubscriber$onNext$1 extends Lambda implements Function0<Unit> {
    final /* synthetic */ Control $control;
    final /* synthetic */ IBinder $token;
    final /* synthetic */ StatefulControlSubscriber this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    StatefulControlSubscriber$onNext$1(StatefulControlSubscriber statefulControlSubscriber, IBinder iBinder, Control control) {
        super(0);
        this.this$0 = statefulControlSubscriber;
        this.$token = iBinder;
        this.$control = control;
    }

    @Override // kotlin.jvm.functions.Function0
    public final void invoke() {
        if (!(this.this$0.subscriptionOpen)) {
            Log.w("StatefulControlSubscriber", "Refresh outside of window for token:" + this.$token);
            return;
        }
        this.this$0.controller.refreshStatus(this.this$0.provider.getComponentName(), this.$control);
    }
}
