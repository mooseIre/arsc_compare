package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.os.Handler;
import com.android.systemui.broadcast.BroadcastDispatcher;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class DeviceProvisionedControllerImpl_Factory implements Factory<DeviceProvisionedControllerImpl> {
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<Context> contextProvider;
    private final Provider<Handler> mainHandlerProvider;

    public DeviceProvisionedControllerImpl_Factory(Provider<Context> provider, Provider<Handler> provider2, Provider<BroadcastDispatcher> provider3) {
        this.contextProvider = provider;
        this.mainHandlerProvider = provider2;
        this.broadcastDispatcherProvider = provider3;
    }

    @Override // javax.inject.Provider
    public DeviceProvisionedControllerImpl get() {
        return provideInstance(this.contextProvider, this.mainHandlerProvider, this.broadcastDispatcherProvider);
    }

    public static DeviceProvisionedControllerImpl provideInstance(Provider<Context> provider, Provider<Handler> provider2, Provider<BroadcastDispatcher> provider3) {
        return new DeviceProvisionedControllerImpl(provider.get(), provider2.get(), provider3.get());
    }

    public static DeviceProvisionedControllerImpl_Factory create(Provider<Context> provider, Provider<Handler> provider2, Provider<BroadcastDispatcher> provider3) {
        return new DeviceProvisionedControllerImpl_Factory(provider, provider2, provider3);
    }
}
