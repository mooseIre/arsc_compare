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

/* access modifiers changed from: package-private */
/* compiled from: MagnetizedObject.kt */
public final /* synthetic */ class MagnetizedObject$animateStuckToTarget$1 extends FunctionReference implements Function5<MagnetizedObject.MagneticTarget, Float, Float, Boolean, Function0<? extends Unit>, Unit> {
    MagnetizedObject$animateStuckToTarget$1(MagnetizedObject magnetizedObject) {
        super(5, magnetizedObject);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getName() {
        return "animateStuckToTargetInternal";
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final KDeclarationContainer getOwner() {
        return Reflection.getOrCreateKotlinClass(MagnetizedObject.class);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getSignature() {
        return "animateStuckToTargetInternal(Lcom/android/systemui/util/magnetictarget/MagnetizedObject$MagneticTarget;FFZLkotlin/jvm/functions/Function0;)V";
    }

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object, java.lang.Object] */
    @Override // kotlin.jvm.functions.Function5
    public /* bridge */ /* synthetic */ Unit invoke(MagnetizedObject.MagneticTarget magneticTarget, Float f, Float f2, Boolean bool, Function0<? extends Unit> function0) {
        invoke(magneticTarget, f.floatValue(), f2.floatValue(), bool.booleanValue(), (Function0<Unit>) function0);
        return Unit.INSTANCE;
    }

    public final void invoke(@NotNull MagnetizedObject.MagneticTarget magneticTarget, float f, float f2, boolean z, @Nullable Function0<Unit> function0) {
        Intrinsics.checkParameterIsNotNull(magneticTarget, "p1");
        ((MagnetizedObject) this.receiver).animateStuckToTargetInternal(magneticTarget, f, f2, z, function0);
    }
}
