package com.android.systemui.controls.management;

import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.controls.controller.ControlsController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ControlsRequestDialog_Factory implements Factory<ControlsRequestDialog> {
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<ControlsController> controllerProvider;
    private final Provider<ControlsListingController> controlsListingControllerProvider;

    public ControlsRequestDialog_Factory(Provider<ControlsController> provider, Provider<BroadcastDispatcher> provider2, Provider<ControlsListingController> provider3) {
        this.controllerProvider = provider;
        this.broadcastDispatcherProvider = provider2;
        this.controlsListingControllerProvider = provider3;
    }

    @Override // javax.inject.Provider
    public ControlsRequestDialog get() {
        return provideInstance(this.controllerProvider, this.broadcastDispatcherProvider, this.controlsListingControllerProvider);
    }

    public static ControlsRequestDialog provideInstance(Provider<ControlsController> provider, Provider<BroadcastDispatcher> provider2, Provider<ControlsListingController> provider3) {
        return new ControlsRequestDialog(provider.get(), provider2.get(), provider3.get());
    }

    public static ControlsRequestDialog_Factory create(Provider<ControlsController> provider, Provider<BroadcastDispatcher> provider2, Provider<ControlsListingController> provider3) {
        return new ControlsRequestDialog_Factory(provider, provider2, provider3);
    }
}
