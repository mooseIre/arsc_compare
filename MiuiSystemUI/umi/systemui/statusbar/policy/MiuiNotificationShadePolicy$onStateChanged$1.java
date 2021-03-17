package com.android.systemui.statusbar.policy;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;

/* compiled from: MiuiNotificationShadePolicy.kt */
final /* synthetic */ class MiuiNotificationShadePolicy$onStateChanged$1 extends FunctionReference implements Function0<Unit> {
    MiuiNotificationShadePolicy$onStateChanged$1(MiuiNotificationShadePolicy miuiNotificationShadePolicy) {
        super(0, miuiNotificationShadePolicy);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getName() {
        return "handleUpdateFsgState";
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(MiuiNotificationShadePolicy.class);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getSignature() {
        return "handleUpdateFsgState()V";
    }

    @Override // kotlin.jvm.functions.Function0
    public final void invoke() {
        ((MiuiNotificationShadePolicy) this.receiver).handleUpdateFsgState();
    }
}
