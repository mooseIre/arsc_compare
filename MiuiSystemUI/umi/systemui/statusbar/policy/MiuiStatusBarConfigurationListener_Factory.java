package com.android.systemui.statusbar.policy;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiStatusBarConfigurationListener_Factory implements Factory<MiuiStatusBarConfigurationListener> {
    private final Provider<ConfigurationController> configurationControllerProvider;
    private final Provider<Context> contextProvider;

    public MiuiStatusBarConfigurationListener_Factory(Provider<ConfigurationController> provider, Provider<Context> provider2) {
        this.configurationControllerProvider = provider;
        this.contextProvider = provider2;
    }

    @Override // javax.inject.Provider
    public MiuiStatusBarConfigurationListener get() {
        return provideInstance(this.configurationControllerProvider, this.contextProvider);
    }

    public static MiuiStatusBarConfigurationListener provideInstance(Provider<ConfigurationController> provider, Provider<Context> provider2) {
        return new MiuiStatusBarConfigurationListener(provider.get(), provider2.get());
    }

    public static MiuiStatusBarConfigurationListener_Factory create(Provider<ConfigurationController> provider, Provider<Context> provider2) {
        return new MiuiStatusBarConfigurationListener_Factory(provider, provider2);
    }
}
