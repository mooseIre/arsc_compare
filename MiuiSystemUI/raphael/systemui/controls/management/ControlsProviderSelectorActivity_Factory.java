package com.android.systemui.controls.management;

import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.controls.controller.ControlsController;
import com.android.systemui.globalactions.GlobalActionsComponent;
import dagger.internal.Factory;
import java.util.concurrent.Executor;
import javax.inject.Provider;

public final class ControlsProviderSelectorActivity_Factory implements Factory<ControlsProviderSelectorActivity> {
    private final Provider<Executor> backExecutorProvider;
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<ControlsController> controlsControllerProvider;
    private final Provider<Executor> executorProvider;
    private final Provider<GlobalActionsComponent> globalActionsComponentProvider;
    private final Provider<ControlsListingController> listingControllerProvider;

    public ControlsProviderSelectorActivity_Factory(Provider<Executor> provider, Provider<Executor> provider2, Provider<ControlsListingController> provider3, Provider<ControlsController> provider4, Provider<GlobalActionsComponent> provider5, Provider<BroadcastDispatcher> provider6) {
        this.executorProvider = provider;
        this.backExecutorProvider = provider2;
        this.listingControllerProvider = provider3;
        this.controlsControllerProvider = provider4;
        this.globalActionsComponentProvider = provider5;
        this.broadcastDispatcherProvider = provider6;
    }

    @Override // javax.inject.Provider
    public ControlsProviderSelectorActivity get() {
        return provideInstance(this.executorProvider, this.backExecutorProvider, this.listingControllerProvider, this.controlsControllerProvider, this.globalActionsComponentProvider, this.broadcastDispatcherProvider);
    }

    public static ControlsProviderSelectorActivity provideInstance(Provider<Executor> provider, Provider<Executor> provider2, Provider<ControlsListingController> provider3, Provider<ControlsController> provider4, Provider<GlobalActionsComponent> provider5, Provider<BroadcastDispatcher> provider6) {
        return new ControlsProviderSelectorActivity(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get());
    }

    public static ControlsProviderSelectorActivity_Factory create(Provider<Executor> provider, Provider<Executor> provider2, Provider<ControlsListingController> provider3, Provider<ControlsController> provider4, Provider<GlobalActionsComponent> provider5, Provider<BroadcastDispatcher> provider6) {
        return new ControlsProviderSelectorActivity_Factory(provider, provider2, provider3, provider4, provider5, provider6);
    }
}
