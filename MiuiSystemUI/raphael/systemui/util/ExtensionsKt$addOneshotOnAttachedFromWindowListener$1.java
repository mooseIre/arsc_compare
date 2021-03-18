package com.android.systemui.util;

import android.view.View;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: Extensions.kt */
public final class ExtensionsKt$addOneshotOnAttachedFromWindowListener$1 implements View.OnAttachStateChangeListener {
    final /* synthetic */ Function1 $onAttached;
    final /* synthetic */ View $this_addOneshotOnAttachedFromWindowListener;

    public void onViewDetachedFromWindow(@NotNull View view) {
        Intrinsics.checkParameterIsNotNull(view, "v");
    }

    ExtensionsKt$addOneshotOnAttachedFromWindowListener$1(View view, Function1 function1) {
        this.$this_addOneshotOnAttachedFromWindowListener = view;
        this.$onAttached = function1;
    }

    public void onViewAttachedToWindow(@NotNull View view) {
        Intrinsics.checkParameterIsNotNull(view, "v");
        this.$this_addOneshotOnAttachedFromWindowListener.removeOnAttachStateChangeListener(this);
        this.$onAttached.invoke(view);
    }
}
