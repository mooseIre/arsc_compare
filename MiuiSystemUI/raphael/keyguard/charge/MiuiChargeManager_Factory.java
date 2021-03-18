package com.android.keyguard.charge;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiChargeManager_Factory implements Factory<MiuiChargeManager> {
    private final Provider<Context> contextProvider;

    public MiuiChargeManager_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public MiuiChargeManager get() {
        return provideInstance(this.contextProvider);
    }

    public static MiuiChargeManager provideInstance(Provider<Context> provider) {
        return new MiuiChargeManager(provider.get());
    }

    public static MiuiChargeManager_Factory create(Provider<Context> provider) {
        return new MiuiChargeManager_Factory(provider);
    }
}
