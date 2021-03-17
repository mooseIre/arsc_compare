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

    public final String getName() {
        return "countFavoritesForComponent";
    }

    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(ControlsController.class);
    }

    public final String getSignature() {
        return "countFavoritesForComponent(Landroid/content/ComponentName;)I";
    }

    public /* bridge */ /* synthetic */ Object invoke(Object obj) {
        return Integer.valueOf(invoke((ComponentName) obj));
    }

    public final int invoke(@NotNull ComponentName componentName) {
        Intrinsics.checkParameterIsNotNull(componentName, "p1");
        return ((ControlsController) this.receiver).countFavoritesForComponent(componentName);
    }
}
