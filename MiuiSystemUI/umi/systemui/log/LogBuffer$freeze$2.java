package com.android.systemui.log;

import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: LogBuffer.kt */
public final class LogBuffer$freeze$2 extends Lambda implements Function1<LogMessage, String> {
    public static final LogBuffer$freeze$2 INSTANCE = new LogBuffer$freeze$2();

    LogBuffer$freeze$2() {
        super(1);
    }

    @NotNull
    public final String invoke(@NotNull LogMessage logMessage) {
        Intrinsics.checkParameterIsNotNull(logMessage, "$receiver");
        return logMessage.getStr1() + " frozen";
    }
}
