package com.android.systemui;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class UiOffloadThread {
    private final ExecutorService mHighExecutorService = Executors.newSingleThreadExecutor();
    private final ExecutorService mNormalExecutorService = Executors.newSingleThreadExecutor();

    public Future<?> execute(Runnable runnable) {
        return this.mNormalExecutorService.submit(runnable);
    }

    public Future<?> submit(Runnable runnable) {
        return submit(runnable, 1);
    }

    public Future<?> submit(Runnable runnable, int i) {
        if (i == 2) {
            return this.mHighExecutorService.submit(runnable);
        }
        return this.mNormalExecutorService.submit(runnable);
    }
}
