package com.android.systemui.util.concurrency;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public interface RepeatableExecutor extends Executor {
    Runnable executeRepeatedly(Runnable runnable, long j, long j2, TimeUnit timeUnit);

    default Runnable executeRepeatedly(Runnable runnable, long j, long j2) {
        return executeRepeatedly(runnable, j, j2, TimeUnit.MILLISECONDS);
    }
}
