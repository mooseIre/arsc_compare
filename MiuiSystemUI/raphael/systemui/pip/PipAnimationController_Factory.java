package com.android.systemui.pip;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class PipAnimationController_Factory implements Factory<PipAnimationController> {
    private final Provider<Context> contextProvider;
    private final Provider<PipSurfaceTransactionHelper> helperProvider;

    public PipAnimationController_Factory(Provider<Context> provider, Provider<PipSurfaceTransactionHelper> provider2) {
        this.contextProvider = provider;
        this.helperProvider = provider2;
    }

    @Override // javax.inject.Provider
    public PipAnimationController get() {
        return provideInstance(this.contextProvider, this.helperProvider);
    }

    public static PipAnimationController provideInstance(Provider<Context> provider, Provider<PipSurfaceTransactionHelper> provider2) {
        return new PipAnimationController(provider.get(), provider2.get());
    }

    public static PipAnimationController_Factory create(Provider<Context> provider, Provider<PipSurfaceTransactionHelper> provider2) {
        return new PipAnimationController_Factory(provider, provider2);
    }
}
