package com.android.systemui.statusbar;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class VibratorHelper_Factory implements Factory<VibratorHelper> {
    private final Provider<Context> contextProvider;

    public VibratorHelper_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public VibratorHelper get() {
        return provideInstance(this.contextProvider);
    }

    public static VibratorHelper provideInstance(Provider<Context> provider) {
        return new VibratorHelper(provider.get());
    }

    public static VibratorHelper_Factory create(Provider<Context> provider) {
        return new VibratorHelper_Factory(provider);
    }
}
