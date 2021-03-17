package com.android.systemui.controls.management;

import android.animation.Animator;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.view.View;
import android.view.ViewGroup;
import java.util.Map;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlsAnimations.kt */
public final class WindowTransition extends Transition {
    @NotNull
    private final Function1<View, Animator> animator;

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: kotlin.jvm.functions.Function1<? super android.view.View, ? extends android.animation.Animator> */
    /* JADX WARN: Multi-variable type inference failed */
    public WindowTransition(@NotNull Function1<? super View, ? extends Animator> function1) {
        Intrinsics.checkParameterIsNotNull(function1, "animator");
        this.animator = function1;
    }

    public void captureStartValues(@NotNull TransitionValues transitionValues) {
        Intrinsics.checkParameterIsNotNull(transitionValues, "tv");
        Map map = transitionValues.values;
        Intrinsics.checkExpressionValueIsNotNull(map, "tv.values");
        map.put("item", Float.valueOf(0.0f));
    }

    public void captureEndValues(@NotNull TransitionValues transitionValues) {
        Intrinsics.checkParameterIsNotNull(transitionValues, "tv");
        Map map = transitionValues.values;
        Intrinsics.checkExpressionValueIsNotNull(map, "tv.values");
        map.put("item", Float.valueOf(1.0f));
    }

    @Nullable
    public Animator createAnimator(@NotNull ViewGroup viewGroup, @Nullable TransitionValues transitionValues, @Nullable TransitionValues transitionValues2) {
        Intrinsics.checkParameterIsNotNull(viewGroup, "sceneRoot");
        Function1<View, Animator> function1 = this.animator;
        if (transitionValues != null) {
            View view = transitionValues.view;
            Intrinsics.checkExpressionValueIsNotNull(view, "startValues!!.view");
            return function1.invoke(view);
        }
        Intrinsics.throwNpe();
        throw null;
    }
}
