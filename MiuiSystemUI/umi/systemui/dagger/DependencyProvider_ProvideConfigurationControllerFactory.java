package com.android.systemui.dagger;

import android.content.Context;
import com.android.systemui.statusbar.policy.ConfigurationController;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class DependencyProvider_ProvideConfigurationControllerFactory implements Factory<ConfigurationController> {
    private final Provider<Context> contextProvider;
    private final DependencyProvider module;

    public DependencyProvider_ProvideConfigurationControllerFactory(DependencyProvider dependencyProvider, Provider<Context> provider) {
        this.module = dependencyProvider;
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public ConfigurationController get() {
        return provideInstance(this.module, this.contextProvider);
    }

    public static ConfigurationController provideInstance(DependencyProvider dependencyProvider, Provider<Context> provider) {
        return proxyProvideConfigurationController(dependencyProvider, provider.get());
    }

    public static DependencyProvider_ProvideConfigurationControllerFactory create(DependencyProvider dependencyProvider, Provider<Context> provider) {
        return new DependencyProvider_ProvideConfigurationControllerFactory(dependencyProvider, provider);
    }

    public static ConfigurationController proxyProvideConfigurationController(DependencyProvider dependencyProvider, Context context) {
        ConfigurationController provideConfigurationController = dependencyProvider.provideConfigurationController(context);
        Preconditions.checkNotNull(provideConfigurationController, "Cannot return null from a non-@Nullable @Provides method");
        return provideConfigurationController;
    }
}
