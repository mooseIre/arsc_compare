package com.android.systemui.util.concurrency;

import android.os.HandlerThread;
import android.os.Looper;
import java.util.concurrent.Executor;

public abstract class ControlCenterConcurrencyModule {
    public static Looper provideCCBgLooper() {
        HandlerThread handlerThread = new HandlerThread("ControlCenterBg", 10);
        handlerThread.start();
        return handlerThread.getLooper();
    }

    public static Executor provideCCBackgroundExecutor(Looper looper) {
        return new ExecutorImpl(looper);
    }
}
