package com.android.systemui.dagger;

import android.content.Context;
import android.net.ConnectivityManager;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class SystemServicesModule_ProvideConnectivityManagagerFactory implements Factory<ConnectivityManager> {
    private final Provider<Context> contextProvider;

    public SystemServicesModule_ProvideConnectivityManagagerFactory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public ConnectivityManager get() {
        return provideInstance(this.contextProvider);
    }

    public static ConnectivityManager provideInstance(Provider<Context> provider) {
        return proxyProvideConnectivityManagager(provider.get());
    }

    public static SystemServicesModule_ProvideConnectivityManagagerFactory create(Provider<Context> provider) {
        return new SystemServicesModule_ProvideConnectivityManagagerFactory(provider);
    }

    public static ConnectivityManager proxyProvideConnectivityManagager(Context context) {
        ConnectivityManager provideConnectivityManagager = SystemServicesModule.provideConnectivityManagager(context);
        Preconditions.checkNotNull(provideConnectivityManagager, "Cannot return null from a non-@Nullable @Provides method");
        return provideConnectivityManagager;
    }
}
