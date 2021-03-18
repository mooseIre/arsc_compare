package com.android.systemui.controls.controller;

import kotlin.jvm.functions.Function0;

/* access modifiers changed from: package-private */
/* compiled from: StatefulControlSubscriber.kt */
public final class StatefulControlSubscriber$run$1 implements Runnable {
    final /* synthetic */ Function0 $f;

    StatefulControlSubscriber$run$1(Function0 function0) {
        this.$f = function0;
    }

    public final void run() {
        this.$f.invoke();
    }
}
