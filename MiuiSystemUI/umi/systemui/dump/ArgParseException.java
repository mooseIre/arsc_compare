package com.android.systemui.dump;

import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: DumpHandler.kt */
public final class ArgParseException extends Exception {
    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ArgParseException(@NotNull String str) {
        super(str);
        Intrinsics.checkParameterIsNotNull(str, "message");
    }
}
