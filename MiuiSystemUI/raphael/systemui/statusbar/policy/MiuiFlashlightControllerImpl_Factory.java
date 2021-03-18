package com.android.systemui.statusbar.policy;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiFlashlightControllerImpl_Factory implements Factory<MiuiFlashlightControllerImpl> {
    private final Provider<Context> contextProvider;

    public MiuiFlashlightControllerImpl_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public MiuiFlashlightControllerImpl get() {
        return provideInstance(this.contextProvider);
    }

    public static MiuiFlashlightControllerImpl provideInstance(Provider<Context> provider) {
        return new MiuiFlashlightControllerImpl(provider.get());
    }

    public static MiuiFlashlightControllerImpl_Factory create(Provider<Context> provider) {
        return new MiuiFlashlightControllerImpl_Factory(provider);
    }
}
