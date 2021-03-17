package com.android.keyguard.utils;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class ExecutorHelper {
    private static volatile ThreadPoolExecutor sCacheThread;

    private ExecutorHelper() {
    }

    public static Executor getIOThreadPool() {
        if (sCacheThread != null) {
            return sCacheThread;
        }
        synchronized (ExecutorHelper.class) {
            if (sCacheThread == null) {
                sCacheThread = new ThreadPoolExecutor(0, 256, 30, TimeUnit.SECONDS, new LinkedBlockingQueue(256), createThreadFactory("Cache"));
            }
        }
        return sCacheThread;
    }

    private static ThreadFactory createThreadFactory(String str) {
        return new ThreadFactory(str) {
            /* class com.android.keyguard.utils.$$Lambda$ExecutorHelper$r1rnX20T1j11sm4Swc0A1mzww0M */
            public final /* synthetic */ String f$0;

            {
                this.f$0 = r1;
            }

            public final Thread newThread(Runnable runnable) {
                return ExecutorHelper.lambda$createThreadFactory$0(this.f$0, runnable);
            }
        };
    }

    static /* synthetic */ Thread lambda$createThreadFactory$0(String str, Runnable runnable) {
        return new InnerThread(str, runnable);
    }

    /* access modifiers changed from: private */
    public static class InnerThread extends Thread {
        InnerThread(String str, Runnable runnable) {
            super(runnable);
            setName(str + "-" + getId());
        }
    }
}
