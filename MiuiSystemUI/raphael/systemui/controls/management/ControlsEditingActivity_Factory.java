package com.android.systemui.controls.management;

import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.controls.controller.ControlsControllerImpl;
import com.android.systemui.globalactions.GlobalActionsComponent;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ControlsEditingActivity_Factory implements Factory<ControlsEditingActivity> {
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<ControlsControllerImpl> controllerProvider;
    private final Provider<GlobalActionsComponent> globalActionsComponentProvider;

    public ControlsEditingActivity_Factory(Provider<ControlsControllerImpl> provider, Provider<BroadcastDispatcher> provider2, Provider<GlobalActionsComponent> provider3) {
        this.controllerProvider = provider;
        this.broadcastDispatcherProvider = provider2;
        this.globalActionsComponentProvider = provider3;
    }

    @Override // javax.inject.Provider
    public ControlsEditingActivity get() {
        return provideInstance(this.controllerProvider, this.broadcastDispatcherProvider, this.globalActionsComponentProvider);
    }

    public static ControlsEditingActivity provideInstance(Provider<ControlsControllerImpl> provider, Provider<BroadcastDispatcher> provider2, Provider<GlobalActionsComponent> provider3) {
        return new ControlsEditingActivity(provider.get(), provider2.get(), provider3.get());
    }

    public static ControlsEditingActivity_Factory create(Provider<ControlsControllerImpl> provider, Provider<BroadcastDispatcher> provider2, Provider<GlobalActionsComponent> provider3) {
        return new ControlsEditingActivity_Factory(provider, provider2, provider3);
    }
}
