package com.android.systemui.dagger;

import com.android.systemui.shared.system.DevicePolicyManagerWrapper;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

public final class DependencyProvider_ProvideDevicePolicyManagerWrapperFactory implements Factory<DevicePolicyManagerWrapper> {
    private final DependencyProvider module;

    public DependencyProvider_ProvideDevicePolicyManagerWrapperFactory(DependencyProvider dependencyProvider) {
        this.module = dependencyProvider;
    }

    @Override // javax.inject.Provider
    public DevicePolicyManagerWrapper get() {
        return provideInstance(this.module);
    }

    public static DevicePolicyManagerWrapper provideInstance(DependencyProvider dependencyProvider) {
        return proxyProvideDevicePolicyManagerWrapper(dependencyProvider);
    }

    public static DependencyProvider_ProvideDevicePolicyManagerWrapperFactory create(DependencyProvider dependencyProvider) {
        return new DependencyProvider_ProvideDevicePolicyManagerWrapperFactory(dependencyProvider);
    }

    public static DevicePolicyManagerWrapper proxyProvideDevicePolicyManagerWrapper(DependencyProvider dependencyProvider) {
        DevicePolicyManagerWrapper provideDevicePolicyManagerWrapper = dependencyProvider.provideDevicePolicyManagerWrapper();
        Preconditions.checkNotNull(provideDevicePolicyManagerWrapper, "Cannot return null from a non-@Nullable @Provides method");
        return provideDevicePolicyManagerWrapper;
    }
}
