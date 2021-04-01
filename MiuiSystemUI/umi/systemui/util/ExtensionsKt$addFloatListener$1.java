package com.android.systemui.util;

import kotlin.jvm.functions.Function1;
import miuix.animation.listener.TransitionListener;
import miuix.animation.property.FloatProperty;
import org.jetbrains.annotations.Nullable;

/* compiled from: Extensions.kt */
public final class ExtensionsKt$addFloatListener$1 extends TransitionListener {
    final /* synthetic */ Function1 $listener;

    ExtensionsKt$addFloatListener$1(Function1 function1) {
        this.$listener = function1;
    }

    @Override // miuix.animation.listener.TransitionListener
    public void onUpdate(@Nullable Object obj, @Nullable FloatProperty<?> floatProperty, float f, float f2, boolean z) {
        this.$listener.invoke(Float.valueOf(f));
    }
}
