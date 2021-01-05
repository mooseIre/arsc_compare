package com.android.systemui.statusbar.policy;

import android.content.Context;
import com.android.systemui.controlcenter.policy.MiuiFlashlightHelper;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class FlashlightControllerImpl_Factory implements Factory<FlashlightControllerImpl> {
    private final Provider<Context> contextProvider;
    private final Provider<MiuiFlashlightHelper> flashlightHelperProvider;

    public FlashlightControllerImpl_Factory(Provider<Context> provider, Provider<MiuiFlashlightHelper> provider2) {
        this.contextProvider = provider;
        this.flashlightHelperProvider = provider2;
    }

    public FlashlightControllerImpl get() {
        return provideInstance(this.contextProvider, this.flashlightHelperProvider);
    }

    public static FlashlightControllerImpl provideInstance(Provider<Context> provider, Provider<MiuiFlashlightHelper> provider2) {
        return new FlashlightControllerImpl(provider.get(), provider2.get());
    }

    public static FlashlightControllerImpl_Factory create(Provider<Context> provider, Provider<MiuiFlashlightHelper> provider2) {
        return new FlashlightControllerImpl_Factory(provider, provider2);
    }
}
