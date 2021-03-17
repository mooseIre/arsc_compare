package com.android.systemui.controls.management;

import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;
import org.jetbrains.annotations.Nullable;

/* access modifiers changed from: package-private */
/* compiled from: ControlAdapter.kt */
public final /* synthetic */ class ControlHolder$accessibilityDelegate$1 extends FunctionReference implements Function1<Boolean, CharSequence> {
    ControlHolder$accessibilityDelegate$1(ControlHolder controlHolder) {
        super(1, controlHolder);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getName() {
        return "stateDescription";
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(ControlHolder.class);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getSignature() {
        return "stateDescription(Z)Ljava/lang/CharSequence;";
    }

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ CharSequence invoke(Boolean bool) {
        return invoke(bool.booleanValue());
    }

    @Nullable
    public final CharSequence invoke(boolean z) {
        return ((ControlHolder) this.receiver).stateDescription(z);
    }
}
