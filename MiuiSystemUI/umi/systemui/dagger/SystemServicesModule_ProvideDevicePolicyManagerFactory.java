package com.android.systemui.dagger;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class SystemServicesModule_ProvideDevicePolicyManagerFactory implements Factory<DevicePolicyManager> {
    private final Provider<Context> contextProvider;

    public SystemServicesModule_ProvideDevicePolicyManagerFactory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public DevicePolicyManager get() {
        return provideInstance(this.contextProvider);
    }

    public static DevicePolicyManager provideInstance(Provider<Context> provider) {
        return proxyProvideDevicePolicyManager(provider.get());
    }

    public static SystemServicesModule_ProvideDevicePolicyManagerFactory create(Provider<Context> provider) {
        return new SystemServicesModule_ProvideDevicePolicyManagerFactory(provider);
    }

    public static DevicePolicyManager proxyProvideDevicePolicyManager(Context context) {
        DevicePolicyManager provideDevicePolicyManager = SystemServicesModule.provideDevicePolicyManager(context);
        Preconditions.checkNotNull(provideDevicePolicyManager, "Cannot return null from a non-@Nullable @Provides method");
        return provideDevicePolicyManager;
    }
}
