package com.android.systemui.util.animation;

import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;
import org.jetbrains.annotations.NotNull;

/* compiled from: PhysicsAnimator.kt */
final /* synthetic */ class PhysicsAnimator$Companion$instanceConstructor$1 extends FunctionReference implements Function1<Object, PhysicsAnimator<Object>> {
    public static final PhysicsAnimator$Companion$instanceConstructor$1 INSTANCE = new PhysicsAnimator$Companion$instanceConstructor$1();

    PhysicsAnimator$Companion$instanceConstructor$1() {
        super(1);
    }

    public final String getName() {
        return "<init>";
    }

    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(PhysicsAnimator.class);
    }

    public final String getSignature() {
        return "<init>(Ljava/lang/Object;)V";
    }

    @NotNull
    public final PhysicsAnimator<Object> invoke(@NotNull Object obj) {
        Intrinsics.checkParameterIsNotNull(obj, "p1");
        return new PhysicsAnimator<>(obj, (DefaultConstructorMarker) null);
    }
}
