package com.android.systemui.util.animation;

import androidx.dynamicanimation.animation.FloatPropertyCompat;
import java.util.Set;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;
import org.jetbrains.annotations.NotNull;

/* compiled from: PhysicsAnimator.kt */
final /* synthetic */ class PhysicsAnimator$cancelAction$1 extends FunctionReference implements Function1<Set<? extends FloatPropertyCompat<? super T>>, Unit> {
    PhysicsAnimator$cancelAction$1(PhysicsAnimator physicsAnimator) {
        super(1, physicsAnimator);
    }

    public final String getName() {
        return "cancelInternal";
    }

    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(PhysicsAnimator.class);
    }

    public final String getSignature() {
        return "cancelInternal$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(Ljava/util/Set;)V";
    }

    public /* bridge */ /* synthetic */ Object invoke(Object obj) {
        invoke((Set) obj);
        return Unit.INSTANCE;
    }

    public final void invoke(@NotNull Set<? extends FloatPropertyCompat<? super T>> set) {
        Intrinsics.checkParameterIsNotNull(set, "p1");
        ((PhysicsAnimator) this.receiver).cancelInternal$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(set);
    }
}
