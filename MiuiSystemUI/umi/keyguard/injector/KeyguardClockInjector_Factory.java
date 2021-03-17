package com.android.keyguard.injector;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class KeyguardClockInjector_Factory implements Factory<KeyguardClockInjector> {
    private final Provider<Context> mContextProvider;

    public KeyguardClockInjector_Factory(Provider<Context> provider) {
        this.mContextProvider = provider;
    }

    @Override // javax.inject.Provider
    public KeyguardClockInjector get() {
        return provideInstance(this.mContextProvider);
    }

    public static KeyguardClockInjector provideInstance(Provider<Context> provider) {
        return new KeyguardClockInjector(provider.get());
    }

    public static KeyguardClockInjector_Factory create(Provider<Context> provider) {
        return new KeyguardClockInjector_Factory(provider);
    }
}
