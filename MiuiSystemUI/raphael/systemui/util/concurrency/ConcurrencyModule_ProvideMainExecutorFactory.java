package com.android.systemui.util.concurrency;

import android.content.Context;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import java.util.concurrent.Executor;
import javax.inject.Provider;

public final class ConcurrencyModule_ProvideMainExecutorFactory implements Factory<Executor> {
    private final Provider<Context> contextProvider;

    public ConcurrencyModule_ProvideMainExecutorFactory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public Executor get() {
        return provideInstance(this.contextProvider);
    }

    public static Executor provideInstance(Provider<Context> provider) {
        return proxyProvideMainExecutor(provider.get());
    }

    public static ConcurrencyModule_ProvideMainExecutorFactory create(Provider<Context> provider) {
        return new ConcurrencyModule_ProvideMainExecutorFactory(provider);
    }

    public static Executor proxyProvideMainExecutor(Context context) {
        Executor provideMainExecutor = ConcurrencyModule.provideMainExecutor(context);
        Preconditions.checkNotNull(provideMainExecutor, "Cannot return null from a non-@Nullable @Provides method");
        return provideMainExecutor;
    }
}
