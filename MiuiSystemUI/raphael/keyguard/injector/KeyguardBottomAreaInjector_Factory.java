package com.android.keyguard.injector;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class KeyguardBottomAreaInjector_Factory implements Factory<KeyguardBottomAreaInjector> {
    private final Provider<Context> mContextProvider;

    public KeyguardBottomAreaInjector_Factory(Provider<Context> provider) {
        this.mContextProvider = provider;
    }

    @Override // javax.inject.Provider
    public KeyguardBottomAreaInjector get() {
        return provideInstance(this.mContextProvider);
    }

    public static KeyguardBottomAreaInjector provideInstance(Provider<Context> provider) {
        return new KeyguardBottomAreaInjector(provider.get());
    }

    public static KeyguardBottomAreaInjector_Factory create(Provider<Context> provider) {
        return new KeyguardBottomAreaInjector_Factory(provider);
    }
}
