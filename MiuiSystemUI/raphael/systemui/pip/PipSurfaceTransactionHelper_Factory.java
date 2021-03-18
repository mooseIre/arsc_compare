package com.android.systemui.pip;

import android.content.Context;
import com.android.systemui.statusbar.policy.ConfigurationController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class PipSurfaceTransactionHelper_Factory implements Factory<PipSurfaceTransactionHelper> {
    private final Provider<ConfigurationController> configControllerProvider;
    private final Provider<Context> contextProvider;

    public PipSurfaceTransactionHelper_Factory(Provider<Context> provider, Provider<ConfigurationController> provider2) {
        this.contextProvider = provider;
        this.configControllerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public PipSurfaceTransactionHelper get() {
        return provideInstance(this.contextProvider, this.configControllerProvider);
    }

    public static PipSurfaceTransactionHelper provideInstance(Provider<Context> provider, Provider<ConfigurationController> provider2) {
        return new PipSurfaceTransactionHelper(provider.get(), provider2.get());
    }

    public static PipSurfaceTransactionHelper_Factory create(Provider<Context> provider, Provider<ConfigurationController> provider2) {
        return new PipSurfaceTransactionHelper_Factory(provider, provider2);
    }
}
