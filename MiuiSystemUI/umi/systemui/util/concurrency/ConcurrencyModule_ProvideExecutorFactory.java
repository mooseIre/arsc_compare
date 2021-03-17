package com.android.systemui.util.concurrency;

import android.os.Looper;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import java.util.concurrent.Executor;
import javax.inject.Provider;

public final class ConcurrencyModule_ProvideExecutorFactory implements Factory<Executor> {
    private final Provider<Looper> looperProvider;

    public ConcurrencyModule_ProvideExecutorFactory(Provider<Looper> provider) {
        this.looperProvider = provider;
    }

    @Override // javax.inject.Provider
    public Executor get() {
        return provideInstance(this.looperProvider);
    }

    public static Executor provideInstance(Provider<Looper> provider) {
        return proxyProvideExecutor(provider.get());
    }

    public static ConcurrencyModule_ProvideExecutorFactory create(Provider<Looper> provider) {
        return new ConcurrencyModule_ProvideExecutorFactory(provider);
    }

    public static Executor proxyProvideExecutor(Looper looper) {
        Executor provideExecutor = ConcurrencyModule.provideExecutor(looper);
        Preconditions.checkNotNull(provideExecutor, "Cannot return null from a non-@Nullable @Provides method");
        return provideExecutor;
    }
}
