package com.android.systemui.dagger;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class SystemServicesModule_ProvideDisplayIdFactory implements Factory<Integer> {
    private final Provider<Context> contextProvider;

    public SystemServicesModule_ProvideDisplayIdFactory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public Integer get() {
        return provideInstance(this.contextProvider);
    }

    public static Integer provideInstance(Provider<Context> provider) {
        return Integer.valueOf(proxyProvideDisplayId(provider.get()));
    }

    public static SystemServicesModule_ProvideDisplayIdFactory create(Provider<Context> provider) {
        return new SystemServicesModule_ProvideDisplayIdFactory(provider);
    }

    public static int proxyProvideDisplayId(Context context) {
        return SystemServicesModule.provideDisplayId(context);
    }
}
