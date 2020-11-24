package com.android.systemui.statusbar.policy;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class FlashlightControllerImpl_Factory implements Factory<FlashlightControllerImpl> {
    private final Provider<Context> contextProvider;

    public FlashlightControllerImpl_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    public FlashlightControllerImpl get() {
        return provideInstance(this.contextProvider);
    }

    public static FlashlightControllerImpl provideInstance(Provider<Context> provider) {
        return new FlashlightControllerImpl(provider.get());
    }

    public static FlashlightControllerImpl_Factory create(Provider<Context> provider) {
        return new FlashlightControllerImpl_Factory(provider);
    }
}
