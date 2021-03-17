package com.android.systemui.dagger;

import android.content.Context;
import android.telephony.TelephonyManager;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class SystemServicesModule_ProvideTelephonyManagerFactory implements Factory<TelephonyManager> {
    private final Provider<Context> contextProvider;

    public SystemServicesModule_ProvideTelephonyManagerFactory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public TelephonyManager get() {
        return provideInstance(this.contextProvider);
    }

    public static TelephonyManager provideInstance(Provider<Context> provider) {
        return proxyProvideTelephonyManager(provider.get());
    }

    public static SystemServicesModule_ProvideTelephonyManagerFactory create(Provider<Context> provider) {
        return new SystemServicesModule_ProvideTelephonyManagerFactory(provider);
    }

    public static TelephonyManager proxyProvideTelephonyManager(Context context) {
        TelephonyManager provideTelephonyManager = SystemServicesModule.provideTelephonyManager(context);
        Preconditions.checkNotNull(provideTelephonyManager, "Cannot return null from a non-@Nullable @Provides method");
        return provideTelephonyManager;
    }
}
