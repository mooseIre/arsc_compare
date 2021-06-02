package com.android.systemui.controls.ui;

import com.android.systemui.C0011R$color;
import kotlin.Pair;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* compiled from: RenderInfo.kt */
final class RenderInfoKt$deviceColorMap$1 extends Lambda implements Function1<Integer, Pair<? extends Integer, ? extends Integer>> {
    public static final RenderInfoKt$deviceColorMap$1 INSTANCE = new RenderInfoKt$deviceColorMap$1();

    RenderInfoKt$deviceColorMap$1() {
        super(1);
    }

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Pair<? extends Integer, ? extends Integer> invoke(Integer num) {
        return invoke(num.intValue());
    }

    @NotNull
    public final Pair<Integer, Integer> invoke(int i) {
        return new Pair<>(Integer.valueOf(C0011R$color.control_foreground), Integer.valueOf(C0011R$color.control_enabled_default_background));
    }
}
