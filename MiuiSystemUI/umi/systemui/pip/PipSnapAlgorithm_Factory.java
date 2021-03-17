package com.android.systemui.pip;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class PipSnapAlgorithm_Factory implements Factory<PipSnapAlgorithm> {
    private final Provider<Context> contextProvider;

    public PipSnapAlgorithm_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public PipSnapAlgorithm get() {
        return provideInstance(this.contextProvider);
    }

    public static PipSnapAlgorithm provideInstance(Provider<Context> provider) {
        return new PipSnapAlgorithm(provider.get());
    }

    public static PipSnapAlgorithm_Factory create(Provider<Context> provider) {
        return new PipSnapAlgorithm_Factory(provider);
    }
}
