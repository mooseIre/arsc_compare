package com.android.systemui.shared.system;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BackgroundExecutor {
    private static final BackgroundExecutor sInstance = new BackgroundExecutor();
    private final ExecutorService mExecutorService = Executors.newFixedThreadPool(2);

    public static BackgroundExecutor get() {
        return sInstance;
    }

    public Future<?> submit(Runnable runnable) {
        return this.mExecutorService.submit(runnable);
    }
}
