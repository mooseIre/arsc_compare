package com.android.systemui.controls.ui;

import java.util.List;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: ControlsUiControllerImpl.kt */
public final /* synthetic */ class ControlsUiControllerImpl$show$5 extends FunctionReference implements Function1<List<? extends SelectionItem>, Unit> {
    ControlsUiControllerImpl$show$5(ControlsUiControllerImpl controlsUiControllerImpl) {
        super(1, controlsUiControllerImpl);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getName() {
        return "showControlsView";
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(ControlsUiControllerImpl.class);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getSignature() {
        return "showControlsView(Ljava/util/List;)V";
    }

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Unit invoke(List<? extends SelectionItem> list) {
        invoke((List<SelectionItem>) list);
        return Unit.INSTANCE;
    }

    public final void invoke(@NotNull List<SelectionItem> list) {
        Intrinsics.checkParameterIsNotNull(list, "p1");
        ((ControlsUiControllerImpl) this.receiver).showControlsView(list);
    }
}
