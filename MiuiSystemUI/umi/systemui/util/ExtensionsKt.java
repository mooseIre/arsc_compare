package com.android.systemui.util;

import android.view.View;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import miuix.animation.IStateStyle;
import org.jetbrains.annotations.NotNull;

/* compiled from: Extensions.kt */
public final class ExtensionsKt {
    public static final void runAfterAttached(@NotNull View view, @NotNull Function1<? super View, Unit> function1) {
        Intrinsics.checkParameterIsNotNull(view, "$this$runAfterAttached");
        Intrinsics.checkParameterIsNotNull(function1, "block");
        if (view.isAttachedToWindow()) {
            function1.invoke(view);
        } else {
            addOneshotOnAttachedFromWindowListener(view, function1);
        }
    }

    public static final void addOneshotOnAttachedFromWindowListener(@NotNull View view, @NotNull Function1<? super View, Unit> function1) {
        Intrinsics.checkParameterIsNotNull(view, "$this$addOneshotOnAttachedFromWindowListener");
        Intrinsics.checkParameterIsNotNull(function1, "onAttached");
        view.addOnAttachStateChangeListener(new ExtensionsKt$addOneshotOnAttachedFromWindowListener$1(view, function1));
    }

    public static final void addFloatListener(@NotNull IStateStyle iStateStyle, @NotNull Function1<? super Float, Unit> function1) {
        Intrinsics.checkParameterIsNotNull(iStateStyle, "$this$addFloatListener");
        Intrinsics.checkParameterIsNotNull(function1, "listener");
        iStateStyle.addListener(new ExtensionsKt$addFloatListener$1(function1));
    }
}
