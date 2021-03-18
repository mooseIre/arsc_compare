package com.android.systemui.dagger;

import android.content.Context;
import android.view.LayoutInflater;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class DependencyProvider_ProviderLayoutInflaterFactory implements Factory<LayoutInflater> {
    private final Provider<Context> contextProvider;
    private final DependencyProvider module;

    public DependencyProvider_ProviderLayoutInflaterFactory(DependencyProvider dependencyProvider, Provider<Context> provider) {
        this.module = dependencyProvider;
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public LayoutInflater get() {
        return provideInstance(this.module, this.contextProvider);
    }

    public static LayoutInflater provideInstance(DependencyProvider dependencyProvider, Provider<Context> provider) {
        return proxyProviderLayoutInflater(dependencyProvider, provider.get());
    }

    public static DependencyProvider_ProviderLayoutInflaterFactory create(DependencyProvider dependencyProvider, Provider<Context> provider) {
        return new DependencyProvider_ProviderLayoutInflaterFactory(dependencyProvider, provider);
    }

    public static LayoutInflater proxyProviderLayoutInflater(DependencyProvider dependencyProvider, Context context) {
        LayoutInflater providerLayoutInflater = dependencyProvider.providerLayoutInflater(context);
        Preconditions.checkNotNull(providerLayoutInflater, "Cannot return null from a non-@Nullable @Provides method");
        return providerLayoutInflater;
    }
}
