package com.android.systemui.util.concurrency;

import android.os.Looper;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class ConcurrencyModule_ProvideDelayableExecutorFactory implements Factory<DelayableExecutor> {
    private final Provider<Looper> looperProvider;

    public ConcurrencyModule_ProvideDelayableExecutorFactory(Provider<Looper> provider) {
        this.looperProvider = provider;
    }

    @Override // javax.inject.Provider
    public DelayableExecutor get() {
        return provideInstance(this.looperProvider);
    }

    public static DelayableExecutor provideInstance(Provider<Looper> provider) {
        return proxyProvideDelayableExecutor(provider.get());
    }

    public static ConcurrencyModule_ProvideDelayableExecutorFactory create(Provider<Looper> provider) {
        return new ConcurrencyModule_ProvideDelayableExecutorFactory(provider);
    }

    public static DelayableExecutor proxyProvideDelayableExecutor(Looper looper) {
        DelayableExecutor provideDelayableExecutor = ConcurrencyModule.provideDelayableExecutor(looper);
        Preconditions.checkNotNull(provideDelayableExecutor, "Cannot return null from a non-@Nullable @Provides method");
        return provideDelayableExecutor;
    }
}
