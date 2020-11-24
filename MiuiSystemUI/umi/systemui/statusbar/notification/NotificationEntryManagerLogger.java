package com.android.systemui.statusbar.notification;

import com.android.systemui.log.LogBuffer;
import com.android.systemui.log.LogLevel;
import com.android.systemui.log.LogMessageImpl;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: NotificationEntryManagerLogger.kt */
public final class NotificationEntryManagerLogger {
    private final LogBuffer buffer;

    public NotificationEntryManagerLogger(@NotNull LogBuffer logBuffer) {
        Intrinsics.checkParameterIsNotNull(logBuffer, "buffer");
        this.buffer = logBuffer;
    }

    public final void logNotifAdded(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        NotificationEntryManagerLogger$logNotifAdded$2 notificationEntryManagerLogger$logNotifAdded$2 = NotificationEntryManagerLogger$logNotifAdded$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("NotificationEntryMgr", logLevel, notificationEntryManagerLogger$logNotifAdded$2);
            obtain.setStr1(str);
            logBuffer.push(obtain);
        }
    }

    public final void logNotifUpdated(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        NotificationEntryManagerLogger$logNotifUpdated$2 notificationEntryManagerLogger$logNotifUpdated$2 = NotificationEntryManagerLogger$logNotifUpdated$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("NotificationEntryMgr", logLevel, notificationEntryManagerLogger$logNotifUpdated$2);
            obtain.setStr1(str);
            logBuffer.push(obtain);
        }
    }

    public final void logInflationAborted(@NotNull String str, @NotNull String str2, @NotNull String str3) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        Intrinsics.checkParameterIsNotNull(str2, "status");
        Intrinsics.checkParameterIsNotNull(str3, "reason");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        NotificationEntryManagerLogger$logInflationAborted$2 notificationEntryManagerLogger$logInflationAborted$2 = NotificationEntryManagerLogger$logInflationAborted$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("NotificationEntryMgr", logLevel, notificationEntryManagerLogger$logInflationAborted$2);
            obtain.setStr1(str);
            obtain.setStr2(str2);
            obtain.setStr3(str3);
            logBuffer.push(obtain);
        }
    }

    public final void logNotifInflated(@NotNull String str, boolean z) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        NotificationEntryManagerLogger$logNotifInflated$2 notificationEntryManagerLogger$logNotifInflated$2 = NotificationEntryManagerLogger$logNotifInflated$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("NotificationEntryMgr", logLevel, notificationEntryManagerLogger$logNotifInflated$2);
            obtain.setStr1(str);
            obtain.setBool1(z);
            logBuffer.push(obtain);
        }
    }

    public final void logRemovalIntercepted(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        NotificationEntryManagerLogger$logRemovalIntercepted$2 notificationEntryManagerLogger$logRemovalIntercepted$2 = NotificationEntryManagerLogger$logRemovalIntercepted$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("NotificationEntryMgr", logLevel, notificationEntryManagerLogger$logRemovalIntercepted$2);
            obtain.setStr1(str);
            logBuffer.push(obtain);
        }
    }

    public final void logLifetimeExtended(@NotNull String str, @NotNull String str2, @NotNull String str3) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        Intrinsics.checkParameterIsNotNull(str2, "extenderName");
        Intrinsics.checkParameterIsNotNull(str3, "status");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        NotificationEntryManagerLogger$logLifetimeExtended$2 notificationEntryManagerLogger$logLifetimeExtended$2 = NotificationEntryManagerLogger$logLifetimeExtended$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("NotificationEntryMgr", logLevel, notificationEntryManagerLogger$logLifetimeExtended$2);
            obtain.setStr1(str);
            obtain.setStr2(str2);
            obtain.setStr3(str3);
            logBuffer.push(obtain);
        }
    }

    public final void logNotifRemoved(@NotNull String str, boolean z) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        NotificationEntryManagerLogger$logNotifRemoved$2 notificationEntryManagerLogger$logNotifRemoved$2 = NotificationEntryManagerLogger$logNotifRemoved$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("NotificationEntryMgr", logLevel, notificationEntryManagerLogger$logNotifRemoved$2);
            obtain.setStr1(str);
            obtain.setBool1(z);
            logBuffer.push(obtain);
        }
    }

    public final void logFilterAndSort(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "reason");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        NotificationEntryManagerLogger$logFilterAndSort$2 notificationEntryManagerLogger$logFilterAndSort$2 = NotificationEntryManagerLogger$logFilterAndSort$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("NotificationEntryMgr", logLevel, notificationEntryManagerLogger$logFilterAndSort$2);
            obtain.setStr1(str);
            logBuffer.push(obtain);
        }
    }
}
