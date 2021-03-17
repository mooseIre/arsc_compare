package com.android.systemui.dagger;

import android.content.Context;
import android.content.SharedPreferences;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class DependencyProvider_ProvideSharePreferencesFactory implements Factory<SharedPreferences> {
    private final Provider<Context> contextProvider;
    private final DependencyProvider module;

    public DependencyProvider_ProvideSharePreferencesFactory(DependencyProvider dependencyProvider, Provider<Context> provider) {
        this.module = dependencyProvider;
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public SharedPreferences get() {
        return provideInstance(this.module, this.contextProvider);
    }

    public static SharedPreferences provideInstance(DependencyProvider dependencyProvider, Provider<Context> provider) {
        return proxyProvideSharePreferences(dependencyProvider, provider.get());
    }

    public static DependencyProvider_ProvideSharePreferencesFactory create(DependencyProvider dependencyProvider, Provider<Context> provider) {
        return new DependencyProvider_ProvideSharePreferencesFactory(dependencyProvider, provider);
    }

    public static SharedPreferences proxyProvideSharePreferences(DependencyProvider dependencyProvider, Context context) {
        SharedPreferences provideSharePreferences = dependencyProvider.provideSharePreferences(context);
        Preconditions.checkNotNull(provideSharePreferences, "Cannot return null from a non-@Nullable @Provides method");
        return provideSharePreferences;
    }
}
