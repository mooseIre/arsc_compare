package com.android.systemui.util.concurrency;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public abstract class ConcurrencyModule {
    public static Looper provideBgLooper() {
        HandlerThread handlerThread = new HandlerThread("SysUiBg", 10);
        handlerThread.start();
        return handlerThread.getLooper();
    }

    public static Looper provideLongRunningLooper() {
        HandlerThread handlerThread = new HandlerThread("SysUiLng", 10);
        handlerThread.start();
        return handlerThread.getLooper();
    }

    public static Looper provideMainLooper() {
        return Looper.getMainLooper();
    }

    public static Handler provideBgHandler(Looper looper) {
        return new Handler(looper);
    }

    public static Handler provideMainHandler(Looper looper) {
        return new Handler(looper);
    }

    @Deprecated
    public static Handler provideHandler() {
        return new Handler();
    }

    public static Executor provideExecutor(Looper looper) {
        return new ExecutorImpl(looper);
    }

    public static Executor provideLongRunningExecutor(Looper looper) {
        return new ExecutorImpl(looper);
    }

    public static Executor provideBackgroundExecutor(Looper looper) {
        return new ExecutorImpl(looper);
    }

    public static Executor provideMainExecutor(Context context) {
        return context.getMainExecutor();
    }

    public static DelayableExecutor provideDelayableExecutor(Looper looper) {
        return new ExecutorImpl(looper);
    }

    public static DelayableExecutor provideBackgroundDelayableExecutor(Looper looper) {
        return new ExecutorImpl(looper);
    }

    public static DelayableExecutor provideMainDelayableExecutor(Looper looper) {
        return new ExecutorImpl(looper);
    }

    public static RepeatableExecutor provideBackgroundRepeatableExecutor(DelayableExecutor delayableExecutor) {
        return new RepeatableExecutorImpl(delayableExecutor);
    }

    public static Executor provideUiBackgroundExecutor() {
        return Executors.newSingleThreadExecutor();
    }
}
