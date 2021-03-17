package com.android.systemui.keyguard;

import dagger.internal.Factory;
import javax.inject.Provider;

public final class KeyguardService_Factory implements Factory<KeyguardService> {
    private final Provider<KeyguardLifecyclesDispatcher> keyguardLifecyclesDispatcherProvider;
    private final Provider<KeyguardViewMediator> keyguardViewMediatorProvider;

    public KeyguardService_Factory(Provider<KeyguardViewMediator> provider, Provider<KeyguardLifecyclesDispatcher> provider2) {
        this.keyguardViewMediatorProvider = provider;
        this.keyguardLifecyclesDispatcherProvider = provider2;
    }

    @Override // javax.inject.Provider
    public KeyguardService get() {
        return provideInstance(this.keyguardViewMediatorProvider, this.keyguardLifecyclesDispatcherProvider);
    }

    public static KeyguardService provideInstance(Provider<KeyguardViewMediator> provider, Provider<KeyguardLifecyclesDispatcher> provider2) {
        return new KeyguardService(provider.get(), provider2.get());
    }

    public static KeyguardService_Factory create(Provider<KeyguardViewMediator> provider, Provider<KeyguardLifecyclesDispatcher> provider2) {
        return new KeyguardService_Factory(provider, provider2);
    }
}
