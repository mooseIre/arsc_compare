package com.android.systemui.statusbar.policy;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class SensorPrivacyControllerImpl_Factory implements Factory<SensorPrivacyControllerImpl> {
    private final Provider<Context> contextProvider;

    public SensorPrivacyControllerImpl_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public SensorPrivacyControllerImpl get() {
        return provideInstance(this.contextProvider);
    }

    public static SensorPrivacyControllerImpl provideInstance(Provider<Context> provider) {
        return new SensorPrivacyControllerImpl(provider.get());
    }

    public static SensorPrivacyControllerImpl_Factory create(Provider<Context> provider) {
        return new SensorPrivacyControllerImpl_Factory(provider);
    }
}
