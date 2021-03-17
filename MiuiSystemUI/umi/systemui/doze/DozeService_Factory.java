package com.android.systemui.doze;

import com.android.systemui.shared.plugins.PluginManager;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class DozeService_Factory implements Factory<DozeService> {
    private final Provider<DozeFactory> dozeFactoryProvider;
    private final Provider<PluginManager> pluginManagerProvider;

    public DozeService_Factory(Provider<DozeFactory> provider, Provider<PluginManager> provider2) {
        this.dozeFactoryProvider = provider;
        this.pluginManagerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public DozeService get() {
        return provideInstance(this.dozeFactoryProvider, this.pluginManagerProvider);
    }

    public static DozeService provideInstance(Provider<DozeFactory> provider, Provider<PluginManager> provider2) {
        return new DozeService(provider.get(), provider2.get());
    }

    public static DozeService_Factory create(Provider<DozeFactory> provider, Provider<PluginManager> provider2) {
        return new DozeService_Factory(provider, provider2);
    }
}
