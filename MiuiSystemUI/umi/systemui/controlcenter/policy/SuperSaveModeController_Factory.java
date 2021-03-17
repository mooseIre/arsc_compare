package com.android.systemui.controlcenter.policy;

import android.content.Context;
import com.miui.systemui.SettingsObserver;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class SuperSaveModeController_Factory implements Factory<SuperSaveModeController> {
    private final Provider<Context> contextProvider;
    private final Provider<SettingsObserver> observerProvider;

    public SuperSaveModeController_Factory(Provider<Context> provider, Provider<SettingsObserver> provider2) {
        this.contextProvider = provider;
        this.observerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public SuperSaveModeController get() {
        return provideInstance(this.contextProvider, this.observerProvider);
    }

    public static SuperSaveModeController provideInstance(Provider<Context> provider, Provider<SettingsObserver> provider2) {
        return new SuperSaveModeController(provider.get(), provider2.get());
    }

    public static SuperSaveModeController_Factory create(Provider<Context> provider, Provider<SettingsObserver> provider2) {
        return new SuperSaveModeController_Factory(provider, provider2);
    }
}
