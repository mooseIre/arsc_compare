package com.android.systemui.dagger;

import android.content.Context;
import com.android.systemui.doze.AlwaysOnDisplayPolicy;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class DependencyProvider_ProvideAlwaysOnDisplayPolicyFactory implements Factory<AlwaysOnDisplayPolicy> {
    private final Provider<Context> contextProvider;
    private final DependencyProvider module;

    public DependencyProvider_ProvideAlwaysOnDisplayPolicyFactory(DependencyProvider dependencyProvider, Provider<Context> provider) {
        this.module = dependencyProvider;
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public AlwaysOnDisplayPolicy get() {
        return provideInstance(this.module, this.contextProvider);
    }

    public static AlwaysOnDisplayPolicy provideInstance(DependencyProvider dependencyProvider, Provider<Context> provider) {
        return proxyProvideAlwaysOnDisplayPolicy(dependencyProvider, provider.get());
    }

    public static DependencyProvider_ProvideAlwaysOnDisplayPolicyFactory create(DependencyProvider dependencyProvider, Provider<Context> provider) {
        return new DependencyProvider_ProvideAlwaysOnDisplayPolicyFactory(dependencyProvider, provider);
    }

    public static AlwaysOnDisplayPolicy proxyProvideAlwaysOnDisplayPolicy(DependencyProvider dependencyProvider, Context context) {
        AlwaysOnDisplayPolicy provideAlwaysOnDisplayPolicy = dependencyProvider.provideAlwaysOnDisplayPolicy(context);
        Preconditions.checkNotNull(provideAlwaysOnDisplayPolicy, "Cannot return null from a non-@Nullable @Provides method");
        return provideAlwaysOnDisplayPolicy;
    }
}
