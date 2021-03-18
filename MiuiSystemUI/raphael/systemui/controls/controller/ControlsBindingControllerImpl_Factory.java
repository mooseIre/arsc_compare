package com.android.systemui.controls.controller;

import android.content.Context;
import com.android.systemui.util.concurrency.DelayableExecutor;
import dagger.internal.DoubleCheck;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ControlsBindingControllerImpl_Factory implements Factory<ControlsBindingControllerImpl> {
    private final Provider<DelayableExecutor> backgroundExecutorProvider;
    private final Provider<Context> contextProvider;
    private final Provider<ControlsController> controllerProvider;

    public ControlsBindingControllerImpl_Factory(Provider<Context> provider, Provider<DelayableExecutor> provider2, Provider<ControlsController> provider3) {
        this.contextProvider = provider;
        this.backgroundExecutorProvider = provider2;
        this.controllerProvider = provider3;
    }

    @Override // javax.inject.Provider
    public ControlsBindingControllerImpl get() {
        return provideInstance(this.contextProvider, this.backgroundExecutorProvider, this.controllerProvider);
    }

    public static ControlsBindingControllerImpl provideInstance(Provider<Context> provider, Provider<DelayableExecutor> provider2, Provider<ControlsController> provider3) {
        return new ControlsBindingControllerImpl(provider.get(), provider2.get(), DoubleCheck.lazy(provider3));
    }

    public static ControlsBindingControllerImpl_Factory create(Provider<Context> provider, Provider<DelayableExecutor> provider2, Provider<ControlsController> provider3) {
        return new ControlsBindingControllerImpl_Factory(provider, provider2, provider3);
    }
}
