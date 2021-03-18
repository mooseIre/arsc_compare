package com.android.systemui.dagger;

import android.content.Context;
import com.android.systemui.shared.plugins.PluginManager;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class DependencyProvider_ProvidePluginManagerFactory implements Factory<PluginManager> {
    private final Provider<Context> contextProvider;
    private final DependencyProvider module;

    public DependencyProvider_ProvidePluginManagerFactory(DependencyProvider dependencyProvider, Provider<Context> provider) {
        this.module = dependencyProvider;
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public PluginManager get() {
        return provideInstance(this.module, this.contextProvider);
    }

    public static PluginManager provideInstance(DependencyProvider dependencyProvider, Provider<Context> provider) {
        return proxyProvidePluginManager(dependencyProvider, provider.get());
    }

    public static DependencyProvider_ProvidePluginManagerFactory create(DependencyProvider dependencyProvider, Provider<Context> provider) {
        return new DependencyProvider_ProvidePluginManagerFactory(dependencyProvider, provider);
    }

    public static PluginManager proxyProvidePluginManager(DependencyProvider dependencyProvider, Context context) {
        PluginManager providePluginManager = dependencyProvider.providePluginManager(context);
        Preconditions.checkNotNull(providePluginManager, "Cannot return null from a non-@Nullable @Provides method");
        return providePluginManager;
    }
}
