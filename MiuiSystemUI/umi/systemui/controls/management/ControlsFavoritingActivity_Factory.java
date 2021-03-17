package com.android.systemui.controls.management;

import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.controls.controller.ControlsControllerImpl;
import com.android.systemui.globalactions.GlobalActionsComponent;
import dagger.internal.Factory;
import java.util.concurrent.Executor;
import javax.inject.Provider;

public final class ControlsFavoritingActivity_Factory implements Factory<ControlsFavoritingActivity> {
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<ControlsControllerImpl> controllerProvider;
    private final Provider<Executor> executorProvider;
    private final Provider<GlobalActionsComponent> globalActionsComponentProvider;
    private final Provider<ControlsListingController> listingControllerProvider;

    public ControlsFavoritingActivity_Factory(Provider<Executor> provider, Provider<ControlsControllerImpl> provider2, Provider<ControlsListingController> provider3, Provider<BroadcastDispatcher> provider4, Provider<GlobalActionsComponent> provider5) {
        this.executorProvider = provider;
        this.controllerProvider = provider2;
        this.listingControllerProvider = provider3;
        this.broadcastDispatcherProvider = provider4;
        this.globalActionsComponentProvider = provider5;
    }

    @Override // javax.inject.Provider
    public ControlsFavoritingActivity get() {
        return provideInstance(this.executorProvider, this.controllerProvider, this.listingControllerProvider, this.broadcastDispatcherProvider, this.globalActionsComponentProvider);
    }

    public static ControlsFavoritingActivity provideInstance(Provider<Executor> provider, Provider<ControlsControllerImpl> provider2, Provider<ControlsListingController> provider3, Provider<BroadcastDispatcher> provider4, Provider<GlobalActionsComponent> provider5) {
        return new ControlsFavoritingActivity(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get());
    }

    public static ControlsFavoritingActivity_Factory create(Provider<Executor> provider, Provider<ControlsControllerImpl> provider2, Provider<ControlsListingController> provider3, Provider<BroadcastDispatcher> provider4, Provider<GlobalActionsComponent> provider5) {
        return new ControlsFavoritingActivity_Factory(provider, provider2, provider3, provider4, provider5);
    }
}
