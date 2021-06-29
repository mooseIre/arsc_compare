package com.android.systemui.dagger;

import com.android.systemui.dump.DumpManager;
import com.android.systemui.log.LogBuffer;
import com.android.systemui.log.LogcatEchoTracker;

public class MiuiLogModule {
    public static LogBuffer providePanelViewLogBuffer(LogcatEchoTracker logcatEchoTracker, DumpManager dumpManager) {
        LogBuffer logBuffer = new LogBuffer("PanelViewLog", 1000, 10, logcatEchoTracker);
        logBuffer.attach(dumpManager);
        return logBuffer;
    }
}
