package com.android.keyguard.injector;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class KeyguardNegative1PageInjector_Factory implements Factory<KeyguardNegative1PageInjector> {
    private final Provider<Context> mContextProvider;

    public KeyguardNegative1PageInjector_Factory(Provider<Context> provider) {
        this.mContextProvider = provider;
    }

    @Override // javax.inject.Provider
    public KeyguardNegative1PageInjector get() {
        return provideInstance(this.mContextProvider);
    }

    public static KeyguardNegative1PageInjector provideInstance(Provider<Context> provider) {
        return new KeyguardNegative1PageInjector(provider.get());
    }

    public static KeyguardNegative1PageInjector_Factory create(Provider<Context> provider) {
        return new KeyguardNegative1PageInjector_Factory(provider);
    }
}
