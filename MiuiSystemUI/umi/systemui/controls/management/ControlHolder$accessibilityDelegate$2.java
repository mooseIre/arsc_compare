package com.android.systemui.controls.management;

import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;

/* access modifiers changed from: package-private */
/* compiled from: ControlAdapter.kt */
public final /* synthetic */ class ControlHolder$accessibilityDelegate$2 extends FunctionReference implements Function0<Integer> {
    ControlHolder$accessibilityDelegate$2(ControlHolder controlHolder) {
        super(0, controlHolder);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getName() {
        return "getLayoutPosition";
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(ControlHolder.class);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getSignature() {
        return "getLayoutPosition()I";
    }

    /* Return type fixed from 'int' to match base method */
    @Override // kotlin.jvm.functions.Function0
    public final Integer invoke() {
        return ((ControlHolder) this.receiver).getLayoutPosition();
    }
}
