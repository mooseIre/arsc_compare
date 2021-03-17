package com.android.systemui.dagger;

import android.content.Context;
import android.telecom.TelecomManager;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class SystemServicesModule_ProvideTelecomManagerFactory implements Factory<TelecomManager> {
    private final Provider<Context> contextProvider;

    public SystemServicesModule_ProvideTelecomManagerFactory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public TelecomManager get() {
        return provideInstance(this.contextProvider);
    }

    public static TelecomManager provideInstance(Provider<Context> provider) {
        return proxyProvideTelecomManager(provider.get());
    }

    public static SystemServicesModule_ProvideTelecomManagerFactory create(Provider<Context> provider) {
        return new SystemServicesModule_ProvideTelecomManagerFactory(provider);
    }

    public static TelecomManager proxyProvideTelecomManager(Context context) {
        return SystemServicesModule.provideTelecomManager(context);
    }
}
