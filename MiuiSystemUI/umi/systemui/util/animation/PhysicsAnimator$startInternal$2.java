package com.android.systemui.util.animation;

import androidx.dynamicanimation.animation.SpringAnimation;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;

/* compiled from: PhysicsAnimator.kt */
final /* synthetic */ class PhysicsAnimator$startInternal$2 extends FunctionReference implements Function0<Unit> {
    PhysicsAnimator$startInternal$2(SpringAnimation springAnimation) {
        super(0, springAnimation);
    }

    public final String getName() {
        return "start";
    }

    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(SpringAnimation.class);
    }

    public final String getSignature() {
        return "start()V";
    }

    public final void invoke() {
        ((SpringAnimation) this.receiver).start();
    }
}
