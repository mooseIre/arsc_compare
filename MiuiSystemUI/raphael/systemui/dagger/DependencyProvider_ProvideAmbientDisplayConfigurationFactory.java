package com.android.systemui.dagger;

import android.content.Context;
import android.hardware.display.AmbientDisplayConfiguration;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class DependencyProvider_ProvideAmbientDisplayConfigurationFactory implements Factory<AmbientDisplayConfiguration> {
    private final Provider<Context> contextProvider;
    private final DependencyProvider module;

    public DependencyProvider_ProvideAmbientDisplayConfigurationFactory(DependencyProvider dependencyProvider, Provider<Context> provider) {
        this.module = dependencyProvider;
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public AmbientDisplayConfiguration get() {
        return provideInstance(this.module, this.contextProvider);
    }

    public static AmbientDisplayConfiguration provideInstance(DependencyProvider dependencyProvider, Provider<Context> provider) {
        return proxyProvideAmbientDisplayConfiguration(dependencyProvider, provider.get());
    }

    public static DependencyProvider_ProvideAmbientDisplayConfigurationFactory create(DependencyProvider dependencyProvider, Provider<Context> provider) {
        return new DependencyProvider_ProvideAmbientDisplayConfigurationFactory(dependencyProvider, provider);
    }

    public static AmbientDisplayConfiguration proxyProvideAmbientDisplayConfiguration(DependencyProvider dependencyProvider, Context context) {
        AmbientDisplayConfiguration provideAmbientDisplayConfiguration = dependencyProvider.provideAmbientDisplayConfiguration(context);
        Preconditions.checkNotNull(provideAmbientDisplayConfiguration, "Cannot return null from a non-@Nullable @Provides method");
        return provideAmbientDisplayConfiguration;
    }
}
