package com.android.systemui.plugins;

import com.android.systemui.shared.plugins.PluginManager;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class PluginDependencyProvider_Factory implements Factory<PluginDependencyProvider> {
    private final Provider<PluginManager> managerProvider;

    public PluginDependencyProvider_Factory(Provider<PluginManager> provider) {
        this.managerProvider = provider;
    }

    @Override // javax.inject.Provider
    public PluginDependencyProvider get() {
        return provideInstance(this.managerProvider);
    }

    public static PluginDependencyProvider provideInstance(Provider<PluginManager> provider) {
        return new PluginDependencyProvider(provider.get());
    }

    public static PluginDependencyProvider_Factory create(Provider<PluginManager> provider) {
        return new PluginDependencyProvider_Factory(provider);
    }

    public static PluginDependencyProvider newPluginDependencyProvider(PluginManager pluginManager) {
        return new PluginDependencyProvider(pluginManager);
    }
}
