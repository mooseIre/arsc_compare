package com.android.systemui.util.concurrency;

import android.os.Looper;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class ConcurrencyModule_ProvideBackgroundDelayableExecutorFactory implements Factory<DelayableExecutor> {
    private final Provider<Looper> looperProvider;

    public ConcurrencyModule_ProvideBackgroundDelayableExecutorFactory(Provider<Looper> provider) {
        this.looperProvider = provider;
    }

    @Override // javax.inject.Provider
    public DelayableExecutor get() {
        return provideInstance(this.looperProvider);
    }

    public static DelayableExecutor provideInstance(Provider<Looper> provider) {
        return proxyProvideBackgroundDelayableExecutor(provider.get());
    }

    public static ConcurrencyModule_ProvideBackgroundDelayableExecutorFactory create(Provider<Looper> provider) {
        return new ConcurrencyModule_ProvideBackgroundDelayableExecutorFactory(provider);
    }

    public static DelayableExecutor proxyProvideBackgroundDelayableExecutor(Looper looper) {
        DelayableExecutor provideBackgroundDelayableExecutor = ConcurrencyModule.provideBackgroundDelayableExecutor(looper);
        Preconditions.checkNotNull(provideBackgroundDelayableExecutor, "Cannot return null from a non-@Nullable @Provides method");
        return provideBackgroundDelayableExecutor;
    }
}
