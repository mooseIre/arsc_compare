package com.android.systemui.dagger;

import android.content.Context;
import android.net.wifi.WifiManager;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class SystemServicesModule_ProvideWifiManagerFactory implements Factory<WifiManager> {
    private final Provider<Context> contextProvider;

    public SystemServicesModule_ProvideWifiManagerFactory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public WifiManager get() {
        return provideInstance(this.contextProvider);
    }

    public static WifiManager provideInstance(Provider<Context> provider) {
        return proxyProvideWifiManager(provider.get());
    }

    public static SystemServicesModule_ProvideWifiManagerFactory create(Provider<Context> provider) {
        return new SystemServicesModule_ProvideWifiManagerFactory(provider);
    }

    public static WifiManager proxyProvideWifiManager(Context context) {
        WifiManager provideWifiManager = SystemServicesModule.provideWifiManager(context);
        Preconditions.checkNotNull(provideWifiManager, "Cannot return null from a non-@Nullable @Provides method");
        return provideWifiManager;
    }
}
