package com.android.systemui.dagger;

import android.content.Context;
import android.hardware.display.NightDisplayListener;
import android.os.Handler;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class DependencyProvider_ProvideNightDisplayListenerFactory implements Factory<NightDisplayListener> {
    private final Provider<Handler> bgHandlerProvider;
    private final Provider<Context> contextProvider;
    private final DependencyProvider module;

    public DependencyProvider_ProvideNightDisplayListenerFactory(DependencyProvider dependencyProvider, Provider<Context> provider, Provider<Handler> provider2) {
        this.module = dependencyProvider;
        this.contextProvider = provider;
        this.bgHandlerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public NightDisplayListener get() {
        return provideInstance(this.module, this.contextProvider, this.bgHandlerProvider);
    }

    public static NightDisplayListener provideInstance(DependencyProvider dependencyProvider, Provider<Context> provider, Provider<Handler> provider2) {
        return proxyProvideNightDisplayListener(dependencyProvider, provider.get(), provider2.get());
    }

    public static DependencyProvider_ProvideNightDisplayListenerFactory create(DependencyProvider dependencyProvider, Provider<Context> provider, Provider<Handler> provider2) {
        return new DependencyProvider_ProvideNightDisplayListenerFactory(dependencyProvider, provider, provider2);
    }

    public static NightDisplayListener proxyProvideNightDisplayListener(DependencyProvider dependencyProvider, Context context, Handler handler) {
        NightDisplayListener provideNightDisplayListener = dependencyProvider.provideNightDisplayListener(context, handler);
        Preconditions.checkNotNull(provideNightDisplayListener, "Cannot return null from a non-@Nullable @Provides method");
        return provideNightDisplayListener;
    }
}
