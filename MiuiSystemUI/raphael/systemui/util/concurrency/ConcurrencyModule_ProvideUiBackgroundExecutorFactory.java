package com.android.systemui.util.concurrency;

import dagger.internal.Factory;
import dagger.internal.Preconditions;
import java.util.concurrent.Executor;

public final class ConcurrencyModule_ProvideUiBackgroundExecutorFactory implements Factory<Executor> {
    private static final ConcurrencyModule_ProvideUiBackgroundExecutorFactory INSTANCE = new ConcurrencyModule_ProvideUiBackgroundExecutorFactory();

    @Override // javax.inject.Provider
    public Executor get() {
        return provideInstance();
    }

    public static Executor provideInstance() {
        return proxyProvideUiBackgroundExecutor();
    }

    public static ConcurrencyModule_ProvideUiBackgroundExecutorFactory create() {
        return INSTANCE;
    }

    public static Executor proxyProvideUiBackgroundExecutor() {
        Executor provideUiBackgroundExecutor = ConcurrencyModule.provideUiBackgroundExecutor();
        Preconditions.checkNotNull(provideUiBackgroundExecutor, "Cannot return null from a non-@Nullable @Provides method");
        return provideUiBackgroundExecutor;
    }
}
