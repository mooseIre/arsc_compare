package com.android.systemui.statusbar.phone;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;

/* compiled from: MiuiNotificationPanelViewController.kt */
final /* synthetic */ class MiuiNotificationPanelViewController$mSetExpandedHeight$1 extends FunctionReference implements Function2<Float, Float, Unit> {
    MiuiNotificationPanelViewController$mSetExpandedHeight$1(MiuiNotificationPanelViewController miuiNotificationPanelViewController) {
        super(2, miuiNotificationPanelViewController);
    }

    public final String getName() {
        return "setAppearFraction";
    }

    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(MiuiNotificationPanelViewController.class);
    }

    public final String getSignature() {
        return "setAppearFraction(FF)V";
    }

    public /* bridge */ /* synthetic */ Object invoke(Object obj, Object obj2) {
        invoke(((Number) obj).floatValue(), ((Number) obj2).floatValue());
        return Unit.INSTANCE;
    }

    public final void invoke(float f, float f2) {
        ((MiuiNotificationPanelViewController) this.receiver).setAppearFraction(f, f2);
    }
}
