package com.android.systemui.dagger;

import android.content.Context;
import android.content.pm.PackageManager;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class SystemServicesModule_ProvidePackageManagerFactory implements Factory<PackageManager> {
    private final Provider<Context> contextProvider;

    public SystemServicesModule_ProvidePackageManagerFactory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public PackageManager get() {
        return provideInstance(this.contextProvider);
    }

    public static PackageManager provideInstance(Provider<Context> provider) {
        return proxyProvidePackageManager(provider.get());
    }

    public static SystemServicesModule_ProvidePackageManagerFactory create(Provider<Context> provider) {
        return new SystemServicesModule_ProvidePackageManagerFactory(provider);
    }

    public static PackageManager proxyProvidePackageManager(Context context) {
        PackageManager providePackageManager = SystemServicesModule.providePackageManager(context);
        Preconditions.checkNotNull(providePackageManager, "Cannot return null from a non-@Nullable @Provides method");
        return providePackageManager;
    }
}
