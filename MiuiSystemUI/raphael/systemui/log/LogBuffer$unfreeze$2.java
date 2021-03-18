package com.android.systemui.log;

import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: LogBuffer.kt */
public final class LogBuffer$unfreeze$2 extends Lambda implements Function1<LogMessage, String> {
    public static final LogBuffer$unfreeze$2 INSTANCE = new LogBuffer$unfreeze$2();

    LogBuffer$unfreeze$2() {
        super(1);
    }

    @NotNull
    public final String invoke(@NotNull LogMessage logMessage) {
        Intrinsics.checkParameterIsNotNull(logMessage, "$receiver");
        return logMessage.getStr1() + " unfrozen";
    }
}
