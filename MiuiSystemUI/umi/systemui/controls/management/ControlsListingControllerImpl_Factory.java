package com.android.systemui.controls.management;

import android.content.Context;
import dagger.internal.Factory;
import java.util.concurrent.Executor;
import javax.inject.Provider;

public final class ControlsListingControllerImpl_Factory implements Factory<ControlsListingControllerImpl> {
    private final Provider<Context> contextProvider;
    private final Provider<Executor> executorProvider;

    public ControlsListingControllerImpl_Factory(Provider<Context> provider, Provider<Executor> provider2) {
        this.contextProvider = provider;
        this.executorProvider = provider2;
    }

    @Override // javax.inject.Provider
    public ControlsListingControllerImpl get() {
        return provideInstance(this.contextProvider, this.executorProvider);
    }

    public static ControlsListingControllerImpl provideInstance(Provider<Context> provider, Provider<Executor> provider2) {
        return new ControlsListingControllerImpl(provider.get(), provider2.get());
    }

    public static ControlsListingControllerImpl_Factory create(Provider<Context> provider, Provider<Executor> provider2) {
        return new ControlsListingControllerImpl_Factory(provider, provider2);
    }
}
