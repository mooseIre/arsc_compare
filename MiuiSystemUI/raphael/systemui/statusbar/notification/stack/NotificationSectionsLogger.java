package com.android.systemui.statusbar.notification.stack;

import com.android.systemui.log.LogBuffer;
import com.android.systemui.log.LogLevel;
import com.android.systemui.log.LogMessageImpl;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: NotificationSectionsLogger.kt */
public final class NotificationSectionsLogger {
    private final LogBuffer logBuffer;

    public NotificationSectionsLogger(@NotNull LogBuffer logBuffer2) {
        Intrinsics.checkParameterIsNotNull(logBuffer2, "logBuffer");
        this.logBuffer = logBuffer2;
    }

    public final void logStartSectionUpdate(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "reason");
        LogBuffer logBuffer2 = this.logBuffer;
        LogLevel logLevel = LogLevel.DEBUG;
        NotificationSectionsLogger$logStartSectionUpdate$2 notificationSectionsLogger$logStartSectionUpdate$2 = new NotificationSectionsLogger$logStartSectionUpdate$2(str);
        if (!logBuffer2.getFrozen()) {
            LogMessageImpl obtain = logBuffer2.obtain("NotifSections", logLevel, notificationSectionsLogger$logStartSectionUpdate$2);
            obtain.setStr1(str);
            logBuffer2.push(obtain);
        }
    }

    public final void logIncomingHeader(int i) {
        logPosition(i, "INCOMING HEADER");
    }

    public final void logMediaControls(int i) {
        logPosition(i, "MEDIA CONTROLS");
    }

    public final void logConversationsHeader(int i) {
        logPosition(i, "CONVERSATIONS HEADER");
    }

    public final void logAlertingHeader(int i) {
        logPosition(i, "ALERTING HEADER");
    }

    public final void logSilentHeader(int i) {
        logPosition(i, "SILENT HEADER");
    }

    public final void logZenModeView(int i) {
        logPosition(i, "ZEN MODE VIEW");
    }

    public final void logImportantView(int i) {
        logPosition(i, "IMPORTANT VIEW");
    }

    public final void logOther(int i, @NotNull Class<?> cls) {
        Intrinsics.checkParameterIsNotNull(cls, "clazz");
        LogBuffer logBuffer2 = this.logBuffer;
        LogLevel logLevel = LogLevel.DEBUG;
        NotificationSectionsLogger$logOther$2 notificationSectionsLogger$logOther$2 = NotificationSectionsLogger$logOther$2.INSTANCE;
        if (!logBuffer2.getFrozen()) {
            LogMessageImpl obtain = logBuffer2.obtain("NotifSections", logLevel, notificationSectionsLogger$logOther$2);
            obtain.setInt1(i);
            obtain.setStr1(cls.getName());
            logBuffer2.push(obtain);
        }
    }

    public final void logHeadsUp(int i, boolean z) {
        logPosition(i, "Heads Up", z);
    }

    public final void logConversation(int i, boolean z) {
        logPosition(i, "Conversation", z);
    }

    public final void logAlerting(int i, boolean z) {
        logPosition(i, "Alerting", z);
    }

    public final void logSilent(int i, boolean z) {
        logPosition(i, "Silent", z);
    }

    public final void logStr(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "str");
        LogBuffer logBuffer2 = this.logBuffer;
        LogLevel logLevel = LogLevel.DEBUG;
        NotificationSectionsLogger$logStr$2 notificationSectionsLogger$logStr$2 = NotificationSectionsLogger$logStr$2.INSTANCE;
        if (!logBuffer2.getFrozen()) {
            LogMessageImpl obtain = logBuffer2.obtain("NotifSections", logLevel, notificationSectionsLogger$logStr$2);
            obtain.setStr1(str);
            logBuffer2.push(obtain);
        }
    }

    private final void logPosition(int i, String str, boolean z) {
        String str2 = z ? " (HUN)" : "";
        LogBuffer logBuffer2 = this.logBuffer;
        LogLevel logLevel = LogLevel.DEBUG;
        NotificationSectionsLogger$logPosition$2 notificationSectionsLogger$logPosition$2 = NotificationSectionsLogger$logPosition$2.INSTANCE;
        if (!logBuffer2.getFrozen()) {
            LogMessageImpl obtain = logBuffer2.obtain("NotifSections", logLevel, notificationSectionsLogger$logPosition$2);
            obtain.setInt1(i);
            obtain.setStr1(str);
            obtain.setStr2(str2);
            logBuffer2.push(obtain);
        }
    }

    private final void logPosition(int i, String str) {
        LogBuffer logBuffer2 = this.logBuffer;
        LogLevel logLevel = LogLevel.DEBUG;
        NotificationSectionsLogger$logPosition$4 notificationSectionsLogger$logPosition$4 = NotificationSectionsLogger$logPosition$4.INSTANCE;
        if (!logBuffer2.getFrozen()) {
            LogMessageImpl obtain = logBuffer2.obtain("NotifSections", logLevel, notificationSectionsLogger$logPosition$4);
            obtain.setInt1(i);
            obtain.setStr1(str);
            logBuffer2.push(obtain);
        }
    }
}
