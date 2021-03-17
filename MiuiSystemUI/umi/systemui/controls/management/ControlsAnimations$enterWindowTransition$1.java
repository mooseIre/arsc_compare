package com.android.systemui.controls.management;

import android.animation.Animator;
import android.view.View;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlsAnimations.kt */
final class ControlsAnimations$enterWindowTransition$1 extends Lambda implements Function1<View, Animator> {
    public static final ControlsAnimations$enterWindowTransition$1 INSTANCE = new ControlsAnimations$enterWindowTransition$1();

    ControlsAnimations$enterWindowTransition$1() {
        super(1);
    }

    @NotNull
    public final Animator invoke(@NotNull View view) {
        Intrinsics.checkParameterIsNotNull(view, "view");
        return ControlsAnimations.INSTANCE.enterAnimation(view);
    }
}
