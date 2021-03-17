package com.android.systemui.statusbar.notification.row;

import com.android.systemui.log.LogBuffer;
import com.android.systemui.log.LogLevel;
import com.android.systemui.log.LogMessageImpl;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: NotifBindPipelineLogger.kt */
public final class NotifBindPipelineLogger {
    private final LogBuffer buffer;

    public NotifBindPipelineLogger(@NotNull LogBuffer logBuffer) {
        Intrinsics.checkParameterIsNotNull(logBuffer, "buffer");
        this.buffer = logBuffer;
    }

    public final void logStageSet(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "stageName");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        NotifBindPipelineLogger$logStageSet$2 notifBindPipelineLogger$logStageSet$2 = NotifBindPipelineLogger$logStageSet$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("NotifBindPipeline", logLevel, notifBindPipelineLogger$logStageSet$2);
            obtain.setStr1(str);
            logBuffer.push(obtain);
        }
    }

    public final void logManagedRow(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "notifKey");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        NotifBindPipelineLogger$logManagedRow$2 notifBindPipelineLogger$logManagedRow$2 = NotifBindPipelineLogger$logManagedRow$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("NotifBindPipeline", logLevel, notifBindPipelineLogger$logManagedRow$2);
            obtain.setStr1(str);
            logBuffer.push(obtain);
        }
    }

    public final void logRequestPipelineRun(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "notifKey");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        NotifBindPipelineLogger$logRequestPipelineRun$2 notifBindPipelineLogger$logRequestPipelineRun$2 = NotifBindPipelineLogger$logRequestPipelineRun$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("NotifBindPipeline", logLevel, notifBindPipelineLogger$logRequestPipelineRun$2);
            obtain.setStr1(str);
            logBuffer.push(obtain);
        }
    }

    public final void logRequestPipelineRowNotSet(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "notifKey");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.WARNING;
        NotifBindPipelineLogger$logRequestPipelineRowNotSet$2 notifBindPipelineLogger$logRequestPipelineRowNotSet$2 = NotifBindPipelineLogger$logRequestPipelineRowNotSet$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("NotifBindPipeline", logLevel, notifBindPipelineLogger$logRequestPipelineRowNotSet$2);
            obtain.setStr1(str);
            logBuffer.push(obtain);
        }
    }

    public final void logStartPipeline(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "notifKey");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        NotifBindPipelineLogger$logStartPipeline$2 notifBindPipelineLogger$logStartPipeline$2 = NotifBindPipelineLogger$logStartPipeline$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("NotifBindPipeline", logLevel, notifBindPipelineLogger$logStartPipeline$2);
            obtain.setStr1(str);
            logBuffer.push(obtain);
        }
    }

    public final void logFinishedPipeline(@NotNull String str, int i) {
        Intrinsics.checkParameterIsNotNull(str, "notifKey");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        NotifBindPipelineLogger$logFinishedPipeline$2 notifBindPipelineLogger$logFinishedPipeline$2 = NotifBindPipelineLogger$logFinishedPipeline$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("NotifBindPipeline", logLevel, notifBindPipelineLogger$logFinishedPipeline$2);
            obtain.setStr1(str);
            obtain.setInt1(i);
            logBuffer.push(obtain);
        }
    }
}
