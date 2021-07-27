package com.android.systemui.controlcenter.phone;

import android.content.Context;
import dagger.internal.Factory;
import java.util.concurrent.Executor;
import javax.inject.Provider;

public final class ExpandInfoControllerImpl_Factory implements Factory<ExpandInfoControllerImpl> {
    private final Provider<Executor> bgExecutorProvider;
    private final Provider<Context> contextProvider;

    public ExpandInfoControllerImpl_Factory(Provider<Context> provider, Provider<Executor> provider2) {
        this.contextProvider = provider;
        this.bgExecutorProvider = provider2;
    }

    @Override // javax.inject.Provider
    public ExpandInfoControllerImpl get() {
        return provideInstance(this.contextProvider, this.bgExecutorProvider);
    }

    public static ExpandInfoControllerImpl provideInstance(Provider<Context> provider, Provider<Executor> provider2) {
        return new ExpandInfoControllerImpl(provider.get(), provider2.get());
    }

    public static ExpandInfoControllerImpl_Factory create(Provider<Context> provider, Provider<Executor> provider2) {
        return new ExpandInfoControllerImpl_Factory(provider, provider2);
    }
}
