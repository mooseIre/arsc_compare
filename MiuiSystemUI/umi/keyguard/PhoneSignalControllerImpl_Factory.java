package com.android.keyguard;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class PhoneSignalControllerImpl_Factory implements Factory<PhoneSignalControllerImpl> {
    private final Provider<Context> contextProvider;

    public PhoneSignalControllerImpl_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public PhoneSignalControllerImpl get() {
        return provideInstance(this.contextProvider);
    }

    public static PhoneSignalControllerImpl provideInstance(Provider<Context> provider) {
        return new PhoneSignalControllerImpl(provider.get());
    }

    public static PhoneSignalControllerImpl_Factory create(Provider<Context> provider) {
        return new PhoneSignalControllerImpl_Factory(provider);
    }
}
