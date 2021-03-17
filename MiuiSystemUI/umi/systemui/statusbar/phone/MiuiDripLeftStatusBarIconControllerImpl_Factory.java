package com.android.systemui.statusbar.phone;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiDripLeftStatusBarIconControllerImpl_Factory implements Factory<MiuiDripLeftStatusBarIconControllerImpl> {
    private final Provider<Context> contextProvider;

    public MiuiDripLeftStatusBarIconControllerImpl_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public MiuiDripLeftStatusBarIconControllerImpl get() {
        return provideInstance(this.contextProvider);
    }

    public static MiuiDripLeftStatusBarIconControllerImpl provideInstance(Provider<Context> provider) {
        return new MiuiDripLeftStatusBarIconControllerImpl(provider.get());
    }

    public static MiuiDripLeftStatusBarIconControllerImpl_Factory create(Provider<Context> provider) {
        return new MiuiDripLeftStatusBarIconControllerImpl_Factory(provider);
    }
}
