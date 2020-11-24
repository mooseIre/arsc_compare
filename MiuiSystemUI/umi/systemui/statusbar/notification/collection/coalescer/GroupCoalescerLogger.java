package com.android.systemui.statusbar.notification.collection.coalescer;

import com.android.systemui.log.LogBuffer;
import com.android.systemui.log.LogLevel;
import com.android.systemui.log.LogMessageImpl;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: GroupCoalescerLogger.kt */
public final class GroupCoalescerLogger {
    private final LogBuffer buffer;

    public GroupCoalescerLogger(@NotNull LogBuffer logBuffer) {
        Intrinsics.checkParameterIsNotNull(logBuffer, "buffer");
        this.buffer = logBuffer;
    }

    public final void logEventCoalesced(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        GroupCoalescerLogger$logEventCoalesced$2 groupCoalescerLogger$logEventCoalesced$2 = GroupCoalescerLogger$logEventCoalesced$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("GroupCoalescer", logLevel, groupCoalescerLogger$logEventCoalesced$2);
            obtain.setStr1(str);
            logBuffer.push(obtain);
        }
    }

    public final void logEmitBatch(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "groupKey");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        GroupCoalescerLogger$logEmitBatch$2 groupCoalescerLogger$logEmitBatch$2 = GroupCoalescerLogger$logEmitBatch$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("GroupCoalescer", logLevel, groupCoalescerLogger$logEmitBatch$2);
            obtain.setStr1(str);
            logBuffer.push(obtain);
        }
    }

    public final void logEarlyEmit(@NotNull String str, @NotNull String str2) {
        Intrinsics.checkParameterIsNotNull(str, "modifiedKey");
        Intrinsics.checkParameterIsNotNull(str2, "groupKey");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        GroupCoalescerLogger$logEarlyEmit$2 groupCoalescerLogger$logEarlyEmit$2 = GroupCoalescerLogger$logEarlyEmit$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("GroupCoalescer", logLevel, groupCoalescerLogger$logEarlyEmit$2);
            obtain.setStr1(str);
            obtain.setStr2(str2);
            logBuffer.push(obtain);
        }
    }

    public final void logMaxBatchTimeout(@NotNull String str, @NotNull String str2) {
        Intrinsics.checkParameterIsNotNull(str, "modifiedKey");
        Intrinsics.checkParameterIsNotNull(str2, "groupKey");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        GroupCoalescerLogger$logMaxBatchTimeout$2 groupCoalescerLogger$logMaxBatchTimeout$2 = GroupCoalescerLogger$logMaxBatchTimeout$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("GroupCoalescer", logLevel, groupCoalescerLogger$logMaxBatchTimeout$2);
            obtain.setStr1(str);
            obtain.setStr2(str2);
            logBuffer.push(obtain);
        }
    }

    public final void logMissingRanking(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "forKey");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.WARNING;
        GroupCoalescerLogger$logMissingRanking$2 groupCoalescerLogger$logMissingRanking$2 = GroupCoalescerLogger$logMissingRanking$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("GroupCoalescer", logLevel, groupCoalescerLogger$logMissingRanking$2);
            obtain.setStr1(str);
            logBuffer.push(obtain);
        }
    }
}
