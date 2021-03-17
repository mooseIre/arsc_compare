package com.android.keyguard;

import android.content.Context;
import com.android.systemui.keyguard.KeyguardViewMediator;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiFastUnlockController_Factory implements Factory<MiuiFastUnlockController> {
    private final Provider<Context> contextProvider;
    private final Provider<KeyguardViewMediator> keyguardViewMediatorProvider;

    public MiuiFastUnlockController_Factory(Provider<Context> provider, Provider<KeyguardViewMediator> provider2) {
        this.contextProvider = provider;
        this.keyguardViewMediatorProvider = provider2;
    }

    @Override // javax.inject.Provider
    public MiuiFastUnlockController get() {
        return provideInstance(this.contextProvider, this.keyguardViewMediatorProvider);
    }

    public static MiuiFastUnlockController provideInstance(Provider<Context> provider, Provider<KeyguardViewMediator> provider2) {
        return new MiuiFastUnlockController(provider.get(), provider2.get());
    }

    public static MiuiFastUnlockController_Factory create(Provider<Context> provider, Provider<KeyguardViewMediator> provider2) {
        return new MiuiFastUnlockController_Factory(provider, provider2);
    }
}
