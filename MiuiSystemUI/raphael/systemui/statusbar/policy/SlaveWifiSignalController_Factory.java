package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.os.Handler;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class SlaveWifiSignalController_Factory implements Factory<SlaveWifiSignalController> {
    private final Provider<Handler> backgroundHandlerProvider;
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<Context> contextProvider;
    private final Provider<Handler> mainHandlerProvider;
    private final Provider<StatusBarIconController> statusBarIconControllerProvider;

    public SlaveWifiSignalController_Factory(Provider<Context> provider, Provider<Handler> provider2, Provider<StatusBarIconController> provider3, Provider<Handler> provider4, Provider<BroadcastDispatcher> provider5) {
        this.contextProvider = provider;
        this.backgroundHandlerProvider = provider2;
        this.statusBarIconControllerProvider = provider3;
        this.mainHandlerProvider = provider4;
        this.broadcastDispatcherProvider = provider5;
    }

    @Override // javax.inject.Provider
    public SlaveWifiSignalController get() {
        return provideInstance(this.contextProvider, this.backgroundHandlerProvider, this.statusBarIconControllerProvider, this.mainHandlerProvider, this.broadcastDispatcherProvider);
    }

    public static SlaveWifiSignalController provideInstance(Provider<Context> provider, Provider<Handler> provider2, Provider<StatusBarIconController> provider3, Provider<Handler> provider4, Provider<BroadcastDispatcher> provider5) {
        return new SlaveWifiSignalController(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get());
    }

    public static SlaveWifiSignalController_Factory create(Provider<Context> provider, Provider<Handler> provider2, Provider<StatusBarIconController> provider3, Provider<Handler> provider4, Provider<BroadcastDispatcher> provider5) {
        return new SlaveWifiSignalController_Factory(provider, provider2, provider3, provider4, provider5);
    }
}
