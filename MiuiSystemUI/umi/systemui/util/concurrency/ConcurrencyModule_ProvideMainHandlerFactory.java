package com.android.systemui.util.concurrency;

import android.os.Handler;
import android.os.Looper;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class ConcurrencyModule_ProvideMainHandlerFactory implements Factory<Handler> {
    private final Provider<Looper> mainLooperProvider;

    public ConcurrencyModule_ProvideMainHandlerFactory(Provider<Looper> provider) {
        this.mainLooperProvider = provider;
    }

    @Override // javax.inject.Provider
    public Handler get() {
        return provideInstance(this.mainLooperProvider);
    }

    public static Handler provideInstance(Provider<Looper> provider) {
        return proxyProvideMainHandler(provider.get());
    }

    public static ConcurrencyModule_ProvideMainHandlerFactory create(Provider<Looper> provider) {
        return new ConcurrencyModule_ProvideMainHandlerFactory(provider);
    }

    public static Handler proxyProvideMainHandler(Looper looper) {
        Handler provideMainHandler = ConcurrencyModule.provideMainHandler(looper);
        Preconditions.checkNotNull(provideMainHandler, "Cannot return null from a non-@Nullable @Provides method");
        return provideMainHandler;
    }
}
