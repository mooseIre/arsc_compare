package com.android.systemui.controls.management;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Lambda;

/* compiled from: ManagementPageIndicator.kt */
final class ManagementPageIndicator$visibilityListener$1 extends Lambda implements Function1<Integer, Unit> {
    public static final ManagementPageIndicator$visibilityListener$1 INSTANCE = new ManagementPageIndicator$visibilityListener$1();

    ManagementPageIndicator$visibilityListener$1() {
        super(1);
    }

    public final void invoke(int i) {
    }

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Unit invoke(Integer num) {
        invoke(num.intValue());
        return Unit.INSTANCE;
    }
}
