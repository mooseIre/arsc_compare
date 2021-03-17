package com.android.systemui.dump;

import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* compiled from: DumpHandler.kt */
final class DumpHandler$parseArgs$2 extends Lambda implements Function1<String, Integer> {
    public static final DumpHandler$parseArgs$2 INSTANCE = new DumpHandler$parseArgs$2();

    DumpHandler$parseArgs$2() {
        super(1);
    }

    public /* bridge */ /* synthetic */ Object invoke(Object obj) {
        return Integer.valueOf(invoke((String) obj));
    }

    public final int invoke(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "it");
        return Integer.parseInt(str);
    }
}
