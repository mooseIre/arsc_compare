package com.android.systemui.log;

import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: LogcatEchoTrackerProd.kt */
public final class LogcatEchoTrackerProd implements LogcatEchoTracker {
    @Override // com.android.systemui.log.LogcatEchoTracker
    public boolean isBufferLoggable(@NotNull String str, @NotNull LogLevel logLevel) {
        Intrinsics.checkParameterIsNotNull(str, "bufferName");
        Intrinsics.checkParameterIsNotNull(logLevel, "level");
        return logLevel.compareTo(LogLevel.WARNING) >= 0;
    }

    @Override // com.android.systemui.log.LogcatEchoTracker
    public boolean isTagLoggable(@NotNull String str, @NotNull LogLevel logLevel) {
        Intrinsics.checkParameterIsNotNull(str, "tagName");
        Intrinsics.checkParameterIsNotNull(logLevel, "level");
        return logLevel.compareTo(LogLevel.WARNING) >= 0;
    }
}
