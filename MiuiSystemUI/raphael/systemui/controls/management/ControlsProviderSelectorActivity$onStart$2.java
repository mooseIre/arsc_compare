package com.android.systemui.controls.management;

import android.content.ComponentName;
import com.android.systemui.controls.controller.ControlsController;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlsProviderSelectorActivity.kt */
final /* synthetic */ class ControlsProviderSelectorActivity$onStart$2 extends FunctionReference implements Function1<ComponentName, Integer> {
    ControlsProviderSelectorActivity$onStart$2(ControlsController controlsController) {
        super(1, controlsController);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getName() {
        return "countFavoritesForComponent";
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(ControlsController.class);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getSignature() {
        return "countFavoritesForComponent(Landroid/content/ComponentName;)I";
    }

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Integer invoke(ComponentName componentName) {
        return Integer.valueOf(invoke(componentName));
    }

    public final int invoke(@NotNull ComponentName componentName) {
        Intrinsics.checkParameterIsNotNull(componentName, "p1");
        return ((ControlsController) this.receiver).countFavoritesForComponent(componentName);
    }
}
