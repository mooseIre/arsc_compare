package com.android.systemui.util.animation;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;

/* compiled from: PhysicsAnimator.kt */
final /* synthetic */ class PhysicsAnimator$withEndActions$1$1 extends FunctionReference implements Function0<Unit> {
    PhysicsAnimator$withEndActions$1$1(Runnable runnable) {
        super(0, runnable);
    }

    public final String getName() {
        return "run";
    }

    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(Runnable.class);
    }

    public final String getSignature() {
        return "run()V";
    }

    public final void invoke() {
        ((Runnable) this.receiver).run();
    }
}
