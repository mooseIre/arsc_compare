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

    public final String getName() {
        return "handleUpdateFsgState";
    }

    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(MiuiNotificationShadePolicy.class);
    }

    public final String getSignature() {
        return "handleUpdateFsgState()V";
    }

    public final void invoke() {
        ((MiuiNotificationShadePolicy) this.receiver).handleUpdateFsgState();
    }
}
