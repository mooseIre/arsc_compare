package com.android.systemui.dagger;

import android.content.pm.IPackageManager;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

public final class SystemServicesModule_ProvideIPackageManagerFactory implements Factory<IPackageManager> {
    private static final SystemServicesModule_ProvideIPackageManagerFactory INSTANCE = new SystemServicesModule_ProvideIPackageManagerFactory();

    @Override // javax.inject.Provider
    public IPackageManager get() {
        return provideInstance();
    }

    public static IPackageManager provideInstance() {
        return proxyProvideIPackageManager();
    }

    public static SystemServicesModule_ProvideIPackageManagerFactory create() {
        return INSTANCE;
    }

    public static IPackageManager proxyProvideIPackageManager() {
        IPackageManager provideIPackageManager = SystemServicesModule.provideIPackageManager();
        Preconditions.checkNotNull(provideIPackageManager, "Cannot return null from a non-@Nullable @Provides method");
        return provideIPackageManager;
    }
}
