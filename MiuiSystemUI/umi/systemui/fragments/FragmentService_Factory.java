package com.android.systemui.fragments;

import com.android.systemui.dagger.SystemUIRootComponent;
import com.android.systemui.statusbar.policy.ConfigurationController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class FragmentService_Factory implements Factory<FragmentService> {
    private final Provider<ConfigurationController> configurationControllerProvider;
    private final Provider<SystemUIRootComponent> rootComponentProvider;

    public FragmentService_Factory(Provider<SystemUIRootComponent> provider, Provider<ConfigurationController> provider2) {
        this.rootComponentProvider = provider;
        this.configurationControllerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public FragmentService get() {
        return provideInstance(this.rootComponentProvider, this.configurationControllerProvider);
    }

    public static FragmentService provideInstance(Provider<SystemUIRootComponent> provider, Provider<ConfigurationController> provider2) {
        return new FragmentService(provider.get(), provider2.get());
    }

    public static FragmentService_Factory create(Provider<SystemUIRootComponent> provider, Provider<ConfigurationController> provider2) {
        return new FragmentService_Factory(provider, provider2);
    }
}
