package com.android.systemui.controls.management;

import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;

/* compiled from: ControlAdapter.kt */
final /* synthetic */ class ControlHolder$accessibilityDelegate$2 extends FunctionReference implements Function0<Integer> {
    ControlHolder$accessibilityDelegate$2(ControlHolder controlHolder) {
        super(0, controlHolder);
    }

    public final String getName() {
        return "getLayoutPosition";
    }

    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(ControlHolder.class);
    }

    public final String getSignature() {
        return "getLayoutPosition()I";
    }

    public final int invoke() {
        return ((ControlHolder) this.receiver).getLayoutPosition();
    }
}
