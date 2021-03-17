package com.android.keyguard.injector;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class KeyguardIndicationInjector_Factory implements Factory<KeyguardIndicationInjector> {
    private final Provider<Context> contextProvider;

    public KeyguardIndicationInjector_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public KeyguardIndicationInjector get() {
        return provideInstance(this.contextProvider);
    }

    public static KeyguardIndicationInjector provideInstance(Provider<Context> provider) {
        return new KeyguardIndicationInjector(provider.get());
    }

    public static KeyguardIndicationInjector_Factory create(Provider<Context> provider) {
        return new KeyguardIndicationInjector_Factory(provider);
    }
}
