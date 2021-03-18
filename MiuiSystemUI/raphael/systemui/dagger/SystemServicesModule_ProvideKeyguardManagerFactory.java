package com.android.systemui.dagger;

import android.app.KeyguardManager;
import android.content.Context;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class SystemServicesModule_ProvideKeyguardManagerFactory implements Factory<KeyguardManager> {
    private final Provider<Context> contextProvider;

    public SystemServicesModule_ProvideKeyguardManagerFactory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public KeyguardManager get() {
        return provideInstance(this.contextProvider);
    }

    public static KeyguardManager provideInstance(Provider<Context> provider) {
        return proxyProvideKeyguardManager(provider.get());
    }

    public static SystemServicesModule_ProvideKeyguardManagerFactory create(Provider<Context> provider) {
        return new SystemServicesModule_ProvideKeyguardManagerFactory(provider);
    }

    public static KeyguardManager proxyProvideKeyguardManager(Context context) {
        KeyguardManager provideKeyguardManager = SystemServicesModule.provideKeyguardManager(context);
        Preconditions.checkNotNull(provideKeyguardManager, "Cannot return null from a non-@Nullable @Provides method");
        return provideKeyguardManager;
    }
}
