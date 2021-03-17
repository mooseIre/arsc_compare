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

/* access modifiers changed from: package-private */
/* compiled from: PhysicsAnimator.kt */
public final /* synthetic */ class PhysicsAnimator$cancelAction$1 extends FunctionReference implements Function1<Set<? extends FloatPropertyCompat<? super T>>, Unit> {
    PhysicsAnimator$cancelAction$1(PhysicsAnimator physicsAnimator) {
        super(1, physicsAnimator);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getName() {
        return "cancelInternal";
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(PhysicsAnimator.class);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getSignature() {
        return "cancelInternal$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(Ljava/util/Set;)V";
    }

    /* Return type fixed from 'java.lang.Object' to match base method */
    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Unit invoke(Object obj) {
        invoke((Set) obj);
        return Unit.INSTANCE;
    }

    public final void invoke(@NotNull Set<? extends FloatPropertyCompat<? super T>> set) {
        Intrinsics.checkParameterIsNotNull(set, "p1");
        ((PhysicsAnimator) this.receiver).cancelInternal$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(set);
    }
}
