package com.android.keyguard.injector;

import android.content.Context;
import com.android.systemui.controlcenter.policy.SuperSaveModeController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class KeyguardUpdateMonitorInjector_Factory implements Factory<KeyguardUpdateMonitorInjector> {
    private final Provider<Context> mContextProvider;
    private final Provider<SuperSaveModeController> mSuperSaveModeControllerProvider;

    public KeyguardUpdateMonitorInjector_Factory(Provider<Context> provider, Provider<SuperSaveModeController> provider2) {
        this.mContextProvider = provider;
        this.mSuperSaveModeControllerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public KeyguardUpdateMonitorInjector get() {
        return provideInstance(this.mContextProvider, this.mSuperSaveModeControllerProvider);
    }

    public static KeyguardUpdateMonitorInjector provideInstance(Provider<Context> provider, Provider<SuperSaveModeController> provider2) {
        return new KeyguardUpdateMonitorInjector(provider.get(), provider2.get());
    }

    public static KeyguardUpdateMonitorInjector_Factory create(Provider<Context> provider, Provider<SuperSaveModeController> provider2) {
        return new KeyguardUpdateMonitorInjector_Factory(provider, provider2);
    }
}
