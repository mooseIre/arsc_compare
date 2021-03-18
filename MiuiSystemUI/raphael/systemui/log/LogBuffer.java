package com.android.systemui.log;

import android.util.Log;
import com.android.systemui.dump.DumpManager;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: LogBuffer.kt */
public final class LogBuffer {
    private final ArrayDeque<LogMessageImpl> buffer = new ArrayDeque<>();
    private boolean frozen;
    private final LogcatEchoTracker logcatEchoTracker;
    private final int maxLogs;
    private final String name;
    private final int poolSize;

    public final /* synthetic */ class WhenMappings {
        public static final /* synthetic */ int[] $EnumSwitchMapping$0;

        static {
            int[] iArr = new int[LogLevel.values().length];
            $EnumSwitchMapping$0 = iArr;
            iArr[LogLevel.VERBOSE.ordinal()] = 1;
            $EnumSwitchMapping$0[LogLevel.DEBUG.ordinal()] = 2;
            $EnumSwitchMapping$0[LogLevel.INFO.ordinal()] = 3;
            $EnumSwitchMapping$0[LogLevel.WARNING.ordinal()] = 4;
            $EnumSwitchMapping$0[LogLevel.ERROR.ordinal()] = 5;
            $EnumSwitchMapping$0[LogLevel.WTF.ordinal()] = 6;
        }
    }

    public LogBuffer(@NotNull String str, int i, int i2, @NotNull LogcatEchoTracker logcatEchoTracker2) {
        Intrinsics.checkParameterIsNotNull(str, "name");
        Intrinsics.checkParameterIsNotNull(logcatEchoTracker2, "logcatEchoTracker");
        this.name = str;
        this.maxLogs = i;
        this.poolSize = i2;
        this.logcatEchoTracker = logcatEchoTracker2;
    }

    public final boolean getFrozen() {
        return this.frozen;
    }

    public final void attach(@NotNull DumpManager dumpManager) {
        Intrinsics.checkParameterIsNotNull(dumpManager, "dumpManager");
        dumpManager.registerBuffer(this.name, this);
    }

    @NotNull
    public final synchronized LogMessageImpl obtain(@NotNull String str, @NotNull LogLevel logLevel, @NotNull Function1<? super LogMessage, String> function1) {
        LogMessageImpl logMessageImpl;
        Intrinsics.checkParameterIsNotNull(str, "tag");
        Intrinsics.checkParameterIsNotNull(logLevel, "level");
        Intrinsics.checkParameterIsNotNull(function1, "printer");
        if (this.frozen) {
            logMessageImpl = LogMessageImpl.Factory.create();
        } else if (this.buffer.size() > this.maxLogs - this.poolSize) {
            logMessageImpl = this.buffer.removeFirst();
        } else {
            logMessageImpl = LogMessageImpl.Factory.create();
        }
        logMessageImpl.reset(str, logLevel, System.currentTimeMillis(), function1);
        Intrinsics.checkExpressionValueIsNotNull(logMessageImpl, "message");
        return logMessageImpl;
    }

    public final synchronized void push(@NotNull LogMessage logMessage) {
        Intrinsics.checkParameterIsNotNull(logMessage, "message");
        if (!this.frozen) {
            if (this.buffer.size() == this.maxLogs) {
                Log.e("LogBuffer", "LogBuffer " + this.name + " has exceeded its pool size");
                this.buffer.removeFirst();
            }
            this.buffer.add((LogMessageImpl) logMessage);
            if (this.logcatEchoTracker.isBufferLoggable(this.name, ((LogMessageImpl) logMessage).getLevel()) || this.logcatEchoTracker.isTagLoggable(((LogMessageImpl) logMessage).getTag(), ((LogMessageImpl) logMessage).getLevel())) {
                echoToLogcat(logMessage);
            }
        }
    }

    public final synchronized void dump(@NotNull PrintWriter printWriter, int i) {
        Intrinsics.checkParameterIsNotNull(printWriter, "pw");
        int i2 = 0;
        int size = i <= 0 ? 0 : this.buffer.size() - i;
        for (T t : this.buffer) {
            if (i2 >= size) {
                Intrinsics.checkExpressionValueIsNotNull(t, "message");
                dumpMessage(t, printWriter);
            }
            i2++;
        }
    }

    public final synchronized void freeze() {
        if (!this.frozen) {
            LogLevel logLevel = LogLevel.DEBUG;
            LogBuffer$freeze$2 logBuffer$freeze$2 = LogBuffer$freeze$2.INSTANCE;
            if (!getFrozen()) {
                LogMessageImpl obtain = obtain("LogBuffer", logLevel, logBuffer$freeze$2);
                obtain.setStr1(this.name);
                push(obtain);
            }
            this.frozen = true;
        }
    }

    public final synchronized void unfreeze() {
        if (this.frozen) {
            LogLevel logLevel = LogLevel.DEBUG;
            LogBuffer$unfreeze$2 logBuffer$unfreeze$2 = LogBuffer$unfreeze$2.INSTANCE;
            if (!getFrozen()) {
                LogMessageImpl obtain = obtain("LogBuffer", logLevel, logBuffer$unfreeze$2);
                obtain.setStr1(this.name);
                push(obtain);
            }
            this.frozen = false;
        }
    }

    private final void dumpMessage(LogMessage logMessage, PrintWriter printWriter) {
        printWriter.print(LogBufferKt.access$getDATE_FORMAT$p().format(Long.valueOf(logMessage.getTimestamp())));
        printWriter.print(" ");
        printWriter.print(logMessage.getLevel());
        printWriter.print(" ");
        printWriter.print(logMessage.getTag());
        printWriter.print(" ");
        printWriter.println(logMessage.getPrinter().invoke(logMessage));
    }

    private final void echoToLogcat(LogMessage logMessage) {
        String invoke = logMessage.getPrinter().invoke(logMessage);
        switch (WhenMappings.$EnumSwitchMapping$0[logMessage.getLevel().ordinal()]) {
            case 1:
                Log.v(logMessage.getTag(), invoke);
                return;
            case 2:
                Log.d(logMessage.getTag(), invoke);
                return;
            case 3:
                Log.i(logMessage.getTag(), invoke);
                return;
            case 4:
                Log.w(logMessage.getTag(), invoke);
                return;
            case 5:
                Log.e(logMessage.getTag(), invoke);
                return;
            case 6:
                Log.wtf(logMessage.getTag(), invoke);
                return;
            default:
                return;
        }
    }
}
