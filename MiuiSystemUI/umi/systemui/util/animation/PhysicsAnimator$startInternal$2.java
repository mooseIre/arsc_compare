package com.android.systemui.util.animation;

import androidx.dynamicanimation.animation.SpringAnimation;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;

/* access modifiers changed from: package-private */
/* compiled from: PhysicsAnimator.kt */
public final /* synthetic */ class PhysicsAnimator$startInternal$2 extends FunctionReference implements Function0<Unit> {
    PhysicsAnimator$startInternal$2(SpringAnimation springAnimation) {
        super(0, springAnimation);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getName() {
        return "start";
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(SpringAnimation.class);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getSignature() {
        return "start()V";
    }

    @Override // kotlin.jvm.functions.Function0
    public final void invoke() {
        ((SpringAnimation) this.receiver).start();
    }
}
