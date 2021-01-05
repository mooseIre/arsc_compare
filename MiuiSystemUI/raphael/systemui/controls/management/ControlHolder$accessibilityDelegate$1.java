package com.android.systemui.controls.management;

import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlAdapter.kt */
final /* synthetic */ class ControlHolder$accessibilityDelegate$1 extends FunctionReference implements Function1<Boolean, CharSequence> {
    ControlHolder$accessibilityDelegate$1(ControlHolder controlHolder) {
        super(1, controlHolder);
    }

    public final String getName() {
        return "stateDescription";
    }

    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(ControlHolder.class);
    }

    public final String getSignature() {
        return "stateDescription(Z)Ljava/lang/CharSequence;";
    }

    public /* bridge */ /* synthetic */ Object invoke(Object obj) {
        return invoke(((Boolean) obj).booleanValue());
    }

    @Nullable
    public final CharSequence invoke(boolean z) {
        return ((ControlHolder) this.receiver).stateDescription(z);
    }
}
