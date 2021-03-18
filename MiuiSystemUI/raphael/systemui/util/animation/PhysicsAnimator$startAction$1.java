package com.android.systemui.util.animation;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;

/* access modifiers changed from: package-private */
/* compiled from: PhysicsAnimator.kt */
public final /* synthetic */ class PhysicsAnimator$startAction$1 extends FunctionReference implements Function0<Unit> {
    PhysicsAnimator$startAction$1(PhysicsAnimator physicsAnimator) {
        super(0, physicsAnimator);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getName() {
        return "startInternal";
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(PhysicsAnimator.class);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getSignature() {
        return "startInternal$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core()V";
    }

    @Override // kotlin.jvm.functions.Function0
    public final void invoke() {
        ((PhysicsAnimator) this.receiver).startInternal$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core();
    }
}
