package com.android.systemui.statusbar.phone;

import android.app.PendingIntent;
import com.android.systemui.log.LogBuffer;
import com.android.systemui.log.LogLevel;
import com.android.systemui.log.LogMessageImpl;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: StatusBarNotificationActivityStarterLogger.kt */
public final class StatusBarNotificationActivityStarterLogger {
    private final LogBuffer buffer;

    public StatusBarNotificationActivityStarterLogger(@NotNull LogBuffer logBuffer) {
        Intrinsics.checkParameterIsNotNull(logBuffer, "buffer");
        this.buffer = logBuffer;
    }

    public final void logStartingActivityFromClick(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        StatusBarNotificationActivityStarterLogger$logStartingActivityFromClick$2 statusBarNotificationActivityStarterLogger$logStartingActivityFromClick$2 = StatusBarNotificationActivityStarterLogger$logStartingActivityFromClick$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("NotifActivityStarter", logLevel, statusBarNotificationActivityStarterLogger$logStartingActivityFromClick$2);
            obtain.setStr1(str);
            logBuffer.push(obtain);
        }
    }

    public final void logHandleClickAfterKeyguardDismissed(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        StatusBarNotificationActivityStarterLogger$logHandleClickAfterKeyguardDismissed$2 statusBarNotificationActivityStarterLogger$logHandleClickAfterKeyguardDismissed$2 = StatusBarNotificationActivityStarterLogger$logHandleClickAfterKeyguardDismissed$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("NotifActivityStarter", logLevel, statusBarNotificationActivityStarterLogger$logHandleClickAfterKeyguardDismissed$2);
            obtain.setStr1(str);
            logBuffer.push(obtain);
        }
    }

    public final void logHandleClickAfterPanelCollapsed(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        StatusBarNotificationActivityStarterLogger$logHandleClickAfterPanelCollapsed$2 statusBarNotificationActivityStarterLogger$logHandleClickAfterPanelCollapsed$2 = StatusBarNotificationActivityStarterLogger$logHandleClickAfterPanelCollapsed$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("NotifActivityStarter", logLevel, statusBarNotificationActivityStarterLogger$logHandleClickAfterPanelCollapsed$2);
            obtain.setStr1(str);
            logBuffer.push(obtain);
        }
    }

    public final void logStartNotificationIntent(@NotNull String str, @NotNull PendingIntent pendingIntent) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        Intrinsics.checkParameterIsNotNull(pendingIntent, "pendingIntent");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        StatusBarNotificationActivityStarterLogger$logStartNotificationIntent$2 statusBarNotificationActivityStarterLogger$logStartNotificationIntent$2 = StatusBarNotificationActivityStarterLogger$logStartNotificationIntent$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("NotifActivityStarter", logLevel, statusBarNotificationActivityStarterLogger$logStartNotificationIntent$2);
            obtain.setStr1(str);
            obtain.setStr2(pendingIntent.getIntent().toString());
            logBuffer.push(obtain);
        }
    }

    public final void logExpandingBubble(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        StatusBarNotificationActivityStarterLogger$logExpandingBubble$2 statusBarNotificationActivityStarterLogger$logExpandingBubble$2 = StatusBarNotificationActivityStarterLogger$logExpandingBubble$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("NotifActivityStarter", logLevel, statusBarNotificationActivityStarterLogger$logExpandingBubble$2);
            obtain.setStr1(str);
            logBuffer.push(obtain);
        }
    }

    public final void logSendingIntentFailed(@NotNull Exception exc) {
        Intrinsics.checkParameterIsNotNull(exc, "e");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.WARNING;
        StatusBarNotificationActivityStarterLogger$logSendingIntentFailed$2 statusBarNotificationActivityStarterLogger$logSendingIntentFailed$2 = StatusBarNotificationActivityStarterLogger$logSendingIntentFailed$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("NotifActivityStarter", logLevel, statusBarNotificationActivityStarterLogger$logSendingIntentFailed$2);
            obtain.setStr1(exc.toString());
            logBuffer.push(obtain);
        }
    }

    public final void logNonClickableNotification(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.ERROR;
        StatusBarNotificationActivityStarterLogger$logNonClickableNotification$2 statusBarNotificationActivityStarterLogger$logNonClickableNotification$2 = StatusBarNotificationActivityStarterLogger$logNonClickableNotification$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("NotifActivityStarter", logLevel, statusBarNotificationActivityStarterLogger$logNonClickableNotification$2);
            obtain.setStr1(str);
            logBuffer.push(obtain);
        }
    }

    public final void logFullScreenIntentSuppressedByDnD(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        StatusBarNotificationActivityStarterLogger$logFullScreenIntentSuppressedByDnD$2 statusBarNotificationActivityStarterLogger$logFullScreenIntentSuppressedByDnD$2 = StatusBarNotificationActivityStarterLogger$logFullScreenIntentSuppressedByDnD$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("NotifActivityStarter", logLevel, statusBarNotificationActivityStarterLogger$logFullScreenIntentSuppressedByDnD$2);
            obtain.setStr1(str);
            logBuffer.push(obtain);
        }
    }

    public final void logFullScreenIntentNotImportantEnough(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        StatusBarNotificationActivityStarterLogger$logFullScreenIntentNotImportantEnough$2 statusBarNotificationActivityStarterLogger$logFullScreenIntentNotImportantEnough$2 = StatusBarNotificationActivityStarterLogger$logFullScreenIntentNotImportantEnough$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("NotifActivityStarter", logLevel, statusBarNotificationActivityStarterLogger$logFullScreenIntentNotImportantEnough$2);
            obtain.setStr1(str);
            logBuffer.push(obtain);
        }
    }

    public final void logSendingFullScreenIntent(@NotNull String str, @NotNull PendingIntent pendingIntent) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        Intrinsics.checkParameterIsNotNull(pendingIntent, "pendingIntent");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.INFO;
        StatusBarNotificationActivityStarterLogger$logSendingFullScreenIntent$2 statusBarNotificationActivityStarterLogger$logSendingFullScreenIntent$2 = StatusBarNotificationActivityStarterLogger$logSendingFullScreenIntent$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("NotifActivityStarter", logLevel, statusBarNotificationActivityStarterLogger$logSendingFullScreenIntent$2);
            obtain.setStr1(str);
            obtain.setStr2(pendingIntent.getIntent().toString());
            logBuffer.push(obtain);
        }
    }
}
