package com.android.systemui.log.dagger;

import android.content.ContentResolver;
import android.os.Build;
import android.os.Looper;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.log.LogBuffer;
import com.android.systemui.log.LogcatEchoTracker;
import com.android.systemui.log.LogcatEchoTrackerDebug;
import com.android.systemui.log.LogcatEchoTrackerProd;

public class LogModule {
    public static LogBuffer provideDozeLogBuffer(LogcatEchoTracker logcatEchoTracker, DumpManager dumpManager) {
        LogBuffer logBuffer = new LogBuffer("DozeLog", 100, 10, logcatEchoTracker);
        logBuffer.attach(dumpManager);
        return logBuffer;
    }

    public static LogBuffer provideNotificationsLogBuffer(LogcatEchoTracker logcatEchoTracker, DumpManager dumpManager) {
        LogBuffer logBuffer = new LogBuffer("NotifLog", 1000, 10, logcatEchoTracker);
        logBuffer.attach(dumpManager);
        return logBuffer;
    }

    public static LogBuffer provideNotificationSectionLogBuffer(LogcatEchoTracker logcatEchoTracker, DumpManager dumpManager) {
        LogBuffer logBuffer = new LogBuffer("NotifSectionLog", 1000, 10, logcatEchoTracker);
        logBuffer.attach(dumpManager);
        return logBuffer;
    }

    public static LogBuffer provideNotifInteractionLogBuffer(LogcatEchoTracker logcatEchoTracker, DumpManager dumpManager) {
        LogBuffer logBuffer = new LogBuffer("NotifInteractionLog", 50, 10, logcatEchoTracker);
        logBuffer.attach(dumpManager);
        return logBuffer;
    }

    public static LogBuffer provideQuickSettingsLogBuffer(LogcatEchoTracker logcatEchoTracker, DumpManager dumpManager) {
        LogBuffer logBuffer = new LogBuffer("QSLog", 500, 10, logcatEchoTracker);
        logBuffer.attach(dumpManager);
        return logBuffer;
    }

    public static LogBuffer provideBroadcastDispatcherLogBuffer(LogcatEchoTracker logcatEchoTracker, DumpManager dumpManager) {
        LogBuffer logBuffer = new LogBuffer("BroadcastDispatcherLog", 500, 10, logcatEchoTracker);
        logBuffer.attach(dumpManager);
        return logBuffer;
    }

    public static LogcatEchoTracker provideLogcatEchoTracker(ContentResolver contentResolver, Looper looper) {
        if (Build.IS_DEBUGGABLE) {
            return LogcatEchoTrackerDebug.create(contentResolver, looper);
        }
        return new LogcatEchoTrackerProd();
    }
}
