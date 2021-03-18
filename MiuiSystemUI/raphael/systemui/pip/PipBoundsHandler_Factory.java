package com.android.systemui.pip;

import android.content.Context;
import com.android.systemui.wm.DisplayController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class PipBoundsHandler_Factory implements Factory<PipBoundsHandler> {
    private final Provider<Context> contextProvider;
    private final Provider<DisplayController> displayControllerProvider;
    private final Provider<PipSnapAlgorithm> pipSnapAlgorithmProvider;

    public PipBoundsHandler_Factory(Provider<Context> provider, Provider<PipSnapAlgorithm> provider2, Provider<DisplayController> provider3) {
        this.contextProvider = provider;
        this.pipSnapAlgorithmProvider = provider2;
        this.displayControllerProvider = provider3;
    }

    @Override // javax.inject.Provider
    public PipBoundsHandler get() {
        return provideInstance(this.contextProvider, this.pipSnapAlgorithmProvider, this.displayControllerProvider);
    }

    public static PipBoundsHandler provideInstance(Provider<Context> provider, Provider<PipSnapAlgorithm> provider2, Provider<DisplayController> provider3) {
        return new PipBoundsHandler(provider.get(), provider2.get(), provider3.get());
    }

    public static PipBoundsHandler_Factory create(Provider<Context> provider, Provider<PipSnapAlgorithm> provider2, Provider<DisplayController> provider3) {
        return new PipBoundsHandler_Factory(provider, provider2, provider3);
    }
}
