package com.android.systemui.dagger;

import android.content.Context;
import android.os.PowerManager;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class SystemServicesModule_ProvidePowerManagerFactory implements Factory<PowerManager> {
    private final Provider<Context> contextProvider;

    public SystemServicesModule_ProvidePowerManagerFactory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public PowerManager get() {
        return provideInstance(this.contextProvider);
    }

    public static PowerManager provideInstance(Provider<Context> provider) {
        return proxyProvidePowerManager(provider.get());
    }

    public static SystemServicesModule_ProvidePowerManagerFactory create(Provider<Context> provider) {
        return new SystemServicesModule_ProvidePowerManagerFactory(provider);
    }

    public static PowerManager proxyProvidePowerManager(Context context) {
        PowerManager providePowerManager = SystemServicesModule.providePowerManager(context);
        Preconditions.checkNotNull(providePowerManager, "Cannot return null from a non-@Nullable @Provides method");
        return providePowerManager;
    }
}
