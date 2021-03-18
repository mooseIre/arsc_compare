package com.android.systemui.dagger;

import android.content.Context;
import android.os.Vibrator;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class SystemServicesModule_ProvideVibratorFactory implements Factory<Vibrator> {
    private final Provider<Context> contextProvider;

    public SystemServicesModule_ProvideVibratorFactory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public Vibrator get() {
        return provideInstance(this.contextProvider);
    }

    public static Vibrator provideInstance(Provider<Context> provider) {
        return proxyProvideVibrator(provider.get());
    }

    public static SystemServicesModule_ProvideVibratorFactory create(Provider<Context> provider) {
        return new SystemServicesModule_ProvideVibratorFactory(provider);
    }

    public static Vibrator proxyProvideVibrator(Context context) {
        return SystemServicesModule.provideVibrator(context);
    }
}
