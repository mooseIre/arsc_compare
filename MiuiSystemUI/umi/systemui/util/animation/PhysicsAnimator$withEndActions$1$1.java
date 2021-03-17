package com.android.systemui.util.animation;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;

/* access modifiers changed from: package-private */
/* compiled from: PhysicsAnimator.kt */
public final /* synthetic */ class PhysicsAnimator$withEndActions$1$1 extends FunctionReference implements Function0<Unit> {
    PhysicsAnimator$withEndActions$1$1(Runnable runnable) {
        super(0, runnable);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getName() {
        return "run";
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(Runnable.class);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getSignature() {
        return "run()V";
    }

    @Override // kotlin.jvm.functions.Function0
    public final void invoke() {
        ((Runnable) this.receiver).run();
    }
}
