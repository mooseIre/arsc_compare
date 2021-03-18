package com.android.keyguard;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class KeyguardSecurityModel_Factory implements Factory<KeyguardSecurityModel> {
    private final Provider<Context> contextProvider;

    public KeyguardSecurityModel_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public KeyguardSecurityModel get() {
        return provideInstance(this.contextProvider);
    }

    public static KeyguardSecurityModel provideInstance(Provider<Context> provider) {
        return new KeyguardSecurityModel(provider.get());
    }

    public static KeyguardSecurityModel_Factory create(Provider<Context> provider) {
        return new KeyguardSecurityModel_Factory(provider);
    }
}
