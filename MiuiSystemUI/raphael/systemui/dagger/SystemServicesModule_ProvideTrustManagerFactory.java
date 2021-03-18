package com.android.systemui.dagger;

import android.app.trust.TrustManager;
import android.content.Context;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class SystemServicesModule_ProvideTrustManagerFactory implements Factory<TrustManager> {
    private final Provider<Context> contextProvider;

    public SystemServicesModule_ProvideTrustManagerFactory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public TrustManager get() {
        return provideInstance(this.contextProvider);
    }

    public static TrustManager provideInstance(Provider<Context> provider) {
        return proxyProvideTrustManager(provider.get());
    }

    public static SystemServicesModule_ProvideTrustManagerFactory create(Provider<Context> provider) {
        return new SystemServicesModule_ProvideTrustManagerFactory(provider);
    }

    public static TrustManager proxyProvideTrustManager(Context context) {
        TrustManager provideTrustManager = SystemServicesModule.provideTrustManager(context);
        Preconditions.checkNotNull(provideTrustManager, "Cannot return null from a non-@Nullable @Provides method");
        return provideTrustManager;
    }
}
