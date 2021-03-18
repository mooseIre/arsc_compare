package com.android.systemui.util.concurrency;

import android.os.Looper;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import java.util.concurrent.Executor;
import javax.inject.Provider;

public final class ConcurrencyModule_ProvideLongRunningExecutorFactory implements Factory<Executor> {
    private final Provider<Looper> looperProvider;

    public ConcurrencyModule_ProvideLongRunningExecutorFactory(Provider<Looper> provider) {
        this.looperProvider = provider;
    }

    @Override // javax.inject.Provider
    public Executor get() {
        return provideInstance(this.looperProvider);
    }

    public static Executor provideInstance(Provider<Looper> provider) {
        return proxyProvideLongRunningExecutor(provider.get());
    }

    public static ConcurrencyModule_ProvideLongRunningExecutorFactory create(Provider<Looper> provider) {
        return new ConcurrencyModule_ProvideLongRunningExecutorFactory(provider);
    }

    public static Executor proxyProvideLongRunningExecutor(Looper looper) {
        Executor provideLongRunningExecutor = ConcurrencyModule.provideLongRunningExecutor(looper);
        Preconditions.checkNotNull(provideLongRunningExecutor, "Cannot return null from a non-@Nullable @Provides method");
        return provideLongRunningExecutor;
    }
}
