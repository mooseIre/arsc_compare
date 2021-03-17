package com.android.systemui.controls.management;

import android.content.ComponentName;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlsProviderSelectorActivity.kt */
final /* synthetic */ class ControlsProviderSelectorActivity$onStart$1 extends FunctionReference implements Function1<ComponentName, Unit> {
    ControlsProviderSelectorActivity$onStart$1(ControlsProviderSelectorActivity controlsProviderSelectorActivity) {
        super(1, controlsProviderSelectorActivity);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getName() {
        return "launchFavoritingActivity";
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(ControlsProviderSelectorActivity.class);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getSignature() {
        return "launchFavoritingActivity(Landroid/content/ComponentName;)V";
    }

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Unit invoke(ComponentName componentName) {
        invoke(componentName);
        return Unit.INSTANCE;
    }

    public final void invoke(@Nullable ComponentName componentName) {
        ((ControlsProviderSelectorActivity) this.receiver).launchFavoritingActivity(componentName);
    }
}
