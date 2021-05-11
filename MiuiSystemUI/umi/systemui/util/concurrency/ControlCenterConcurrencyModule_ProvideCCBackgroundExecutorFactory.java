package com.android.systemui.util.concurrency;

import android.os.Looper;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import java.util.concurrent.Executor;
import javax.inject.Provider;

public final class ControlCenterConcurrencyModule_ProvideCCBackgroundExecutorFactory implements Factory<Executor> {
    private final Provider<Looper> looperProvider;

    public ControlCenterConcurrencyModule_ProvideCCBackgroundExecutorFactory(Provider<Looper> provider) {
        this.looperProvider = provider;
    }

    @Override // javax.inject.Provider
    public Executor get() {
        return provideInstance(this.looperProvider);
    }

    public static Executor provideInstance(Provider<Looper> provider) {
        return proxyProvideCCBackgroundExecutor(provider.get());
    }

    public static ControlCenterConcurrencyModule_ProvideCCBackgroundExecutorFactory create(Provider<Looper> provider) {
        return new ControlCenterConcurrencyModule_ProvideCCBackgroundExecutorFactory(provider);
    }

    public static Executor proxyProvideCCBackgroundExecutor(Looper looper) {
        Executor provideCCBackgroundExecutor = ControlCenterConcurrencyModule.provideCCBackgroundExecutor(looper);
        Preconditions.checkNotNull(provideCCBackgroundExecutor, "Cannot return null from a non-@Nullable @Provides method");
        return provideCCBackgroundExecutor;
    }
}
