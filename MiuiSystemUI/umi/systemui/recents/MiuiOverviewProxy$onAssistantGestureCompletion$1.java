package com.android.systemui.recents;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;

/* compiled from: MiuiOverviewProxy.kt */
final /* synthetic */ class MiuiOverviewProxy$onAssistantGestureCompletion$1 extends FunctionReference implements Function0<Unit> {
    MiuiOverviewProxy$onAssistantGestureCompletion$1(MiuiOverviewProxy miuiOverviewProxy) {
        super(0, miuiOverviewProxy);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getName() {
        return "notifyCompleteAssistant";
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(MiuiOverviewProxy.class);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getSignature() {
        return "notifyCompleteAssistant()V";
    }

    @Override // kotlin.jvm.functions.Function0
    public final void invoke() {
        MiuiOverviewProxy.access$notifyCompleteAssistant((MiuiOverviewProxy) this.receiver);
    }
}
