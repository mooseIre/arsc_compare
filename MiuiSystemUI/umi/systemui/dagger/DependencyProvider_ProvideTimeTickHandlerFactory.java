package com.android.systemui.dagger;

import android.os.Handler;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

public final class DependencyProvider_ProvideTimeTickHandlerFactory implements Factory<Handler> {
    private final DependencyProvider module;

    public DependencyProvider_ProvideTimeTickHandlerFactory(DependencyProvider dependencyProvider) {
        this.module = dependencyProvider;
    }

    @Override // javax.inject.Provider
    public Handler get() {
        return provideInstance(this.module);
    }

    public static Handler provideInstance(DependencyProvider dependencyProvider) {
        return proxyProvideTimeTickHandler(dependencyProvider);
    }

    public static DependencyProvider_ProvideTimeTickHandlerFactory create(DependencyProvider dependencyProvider) {
        return new DependencyProvider_ProvideTimeTickHandlerFactory(dependencyProvider);
    }

    public static Handler proxyProvideTimeTickHandler(DependencyProvider dependencyProvider) {
        Handler provideTimeTickHandler = dependencyProvider.provideTimeTickHandler();
        Preconditions.checkNotNull(provideTimeTickHandler, "Cannot return null from a non-@Nullable @Provides method");
        return provideTimeTickHandler;
    }
}
