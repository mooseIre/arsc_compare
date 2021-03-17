package com.android.systemui.util.concurrency;

import android.os.Handler;
import android.os.Looper;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class ConcurrencyModule_ProvideBgHandlerFactory implements Factory<Handler> {
    private final Provider<Looper> bgLooperProvider;

    public ConcurrencyModule_ProvideBgHandlerFactory(Provider<Looper> provider) {
        this.bgLooperProvider = provider;
    }

    @Override // javax.inject.Provider
    public Handler get() {
        return provideInstance(this.bgLooperProvider);
    }

    public static Handler provideInstance(Provider<Looper> provider) {
        return proxyProvideBgHandler(provider.get());
    }

    public static ConcurrencyModule_ProvideBgHandlerFactory create(Provider<Looper> provider) {
        return new ConcurrencyModule_ProvideBgHandlerFactory(provider);
    }

    public static Handler proxyProvideBgHandler(Looper looper) {
        Handler provideBgHandler = ConcurrencyModule.provideBgHandler(looper);
        Preconditions.checkNotNull(provideBgHandler, "Cannot return null from a non-@Nullable @Provides method");
        return provideBgHandler;
    }
}
