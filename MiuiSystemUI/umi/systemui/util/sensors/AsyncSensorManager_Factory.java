package com.android.systemui.util.sensors;

import android.content.Context;
import com.android.systemui.shared.plugins.PluginManager;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class AsyncSensorManager_Factory implements Factory<AsyncSensorManager> {
    private final Provider<Context> contextProvider;
    private final Provider<PluginManager> pluginManagerProvider;

    public AsyncSensorManager_Factory(Provider<Context> provider, Provider<PluginManager> provider2) {
        this.contextProvider = provider;
        this.pluginManagerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public AsyncSensorManager get() {
        return provideInstance(this.contextProvider, this.pluginManagerProvider);
    }

    public static AsyncSensorManager provideInstance(Provider<Context> provider, Provider<PluginManager> provider2) {
        return new AsyncSensorManager(provider.get(), provider2.get());
    }

    public static AsyncSensorManager_Factory create(Provider<Context> provider, Provider<PluginManager> provider2) {
        return new AsyncSensorManager_Factory(provider, provider2);
    }
}
