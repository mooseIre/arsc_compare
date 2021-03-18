package com.android.systemui.dagger;

import android.content.Context;
import android.content.res.Resources;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class SystemServicesModule_ProvideResourcesFactory implements Factory<Resources> {
    private final Provider<Context> contextProvider;

    public SystemServicesModule_ProvideResourcesFactory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public Resources get() {
        return provideInstance(this.contextProvider);
    }

    public static Resources provideInstance(Provider<Context> provider) {
        return proxyProvideResources(provider.get());
    }

    public static SystemServicesModule_ProvideResourcesFactory create(Provider<Context> provider) {
        return new SystemServicesModule_ProvideResourcesFactory(provider);
    }

    public static Resources proxyProvideResources(Context context) {
        Resources provideResources = SystemServicesModule.provideResources(context);
        Preconditions.checkNotNull(provideResources, "Cannot return null from a non-@Nullable @Provides method");
        return provideResources;
    }
}
