package com.android.systemui.statusbar.phone;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;

/* access modifiers changed from: package-private */
/* compiled from: MiuiNotificationPanelViewController.kt */
public final /* synthetic */ class MiuiNotificationPanelViewController$mSetExpandedHeight$1 extends FunctionReference implements Function2<Float, Float, Unit> {
    MiuiNotificationPanelViewController$mSetExpandedHeight$1(MiuiNotificationPanelViewController miuiNotificationPanelViewController) {
        super(2, miuiNotificationPanelViewController);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getName() {
        return "setAppearFraction";
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(MiuiNotificationPanelViewController.class);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getSignature() {
        return "setAppearFraction(FF)V";
    }

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object, java.lang.Object] */
    @Override // kotlin.jvm.functions.Function2
    public /* bridge */ /* synthetic */ Unit invoke(Float f, Float f2) {
        invoke(f.floatValue(), f2.floatValue());
        return Unit.INSTANCE;
    }

    public final void invoke(float f, float f2) {
        ((MiuiNotificationPanelViewController) this.receiver).setAppearFraction(f, f2);
    }
}
