package com.android.systemui.controlcenter.policy;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiFlashlightHelper_Factory implements Factory<MiuiFlashlightHelper> {
    private final Provider<Context> contextProvider;

    public MiuiFlashlightHelper_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public MiuiFlashlightHelper get() {
        return provideInstance(this.contextProvider);
    }

    public static MiuiFlashlightHelper provideInstance(Provider<Context> provider) {
        return new MiuiFlashlightHelper(provider.get());
    }

    public static MiuiFlashlightHelper_Factory create(Provider<Context> provider) {
        return new MiuiFlashlightHelper_Factory(provider);
    }
}
