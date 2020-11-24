package com.android.systemui.util.magnetictarget;

import com.android.systemui.util.magnetictarget.MagnetizedObject;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function5;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MagnetizedObject.kt */
final /* synthetic */ class MagnetizedObject$animateStuckToTarget$1 extends FunctionReference implements Function5<MagnetizedObject.MagneticTarget, Float, Float, Boolean, Function0<? extends Unit>, Unit> {
    MagnetizedObject$animateStuckToTarget$1(MagnetizedObject magnetizedObject) {
        super(5, magnetizedObject);
    }

    public final String getName() {
        return "animateStuckToTargetInternal";
    }

    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(MagnetizedObject.class);
    }

    public final String getSignature() {
        return "animateStuckToTargetInternal(Lcom/android/systemui/util/magnetictarget/MagnetizedObject$MagneticTarget;FFZLkotlin/jvm/functions/Function0;)V";
    }

    public /* bridge */ /* synthetic */ Object invoke(Object obj, Object obj2, Object obj3, Object obj4, Object obj5) {
        invoke((MagnetizedObject.MagneticTarget) obj, ((Number) obj2).floatValue(), ((Number) obj3).floatValue(), ((Boolean) obj4).booleanValue(), (Function0<Unit>) (Function0) obj5);
        return Unit.INSTANCE;
    }

    public final void invoke(@NotNull MagnetizedObject.MagneticTarget magneticTarget, float f, float f2, boolean z, @Nullable Function0<Unit> function0) {
        Intrinsics.checkParameterIsNotNull(magneticTarget, "p1");
        ((MagnetizedObject) this.receiver).animateStuckToTargetInternal(magneticTarget, f, f2, z, function0);
    }
}
