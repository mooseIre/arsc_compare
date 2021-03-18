package com.android.systemui.dagger;

import android.app.IActivityManager;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

public final class SystemServicesModule_ProvideIActivityManagerFactory implements Factory<IActivityManager> {
    private static final SystemServicesModule_ProvideIActivityManagerFactory INSTANCE = new SystemServicesModule_ProvideIActivityManagerFactory();

    @Override // javax.inject.Provider
    public IActivityManager get() {
        return provideInstance();
    }

    public static IActivityManager provideInstance() {
        return proxyProvideIActivityManager();
    }

    public static SystemServicesModule_ProvideIActivityManagerFactory create() {
        return INSTANCE;
    }

    public static IActivityManager proxyProvideIActivityManager() {
        IActivityManager provideIActivityManager = SystemServicesModule.provideIActivityManager();
        Preconditions.checkNotNull(provideIActivityManager, "Cannot return null from a non-@Nullable @Provides method");
        return provideIActivityManager;
    }
}
