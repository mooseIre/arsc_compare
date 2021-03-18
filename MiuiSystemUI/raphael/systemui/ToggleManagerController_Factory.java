package com.android.systemui;

import android.content.Context;
import android.os.Handler;
import com.android.systemui.broadcast.BroadcastDispatcher;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ToggleManagerController_Factory implements Factory<ToggleManagerController> {
    private final Provider<Handler> bgHandlerProvider;
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<Context> contextProvider;

    public ToggleManagerController_Factory(Provider<Context> provider, Provider<BroadcastDispatcher> provider2, Provider<Handler> provider3) {
        this.contextProvider = provider;
        this.broadcastDispatcherProvider = provider2;
        this.bgHandlerProvider = provider3;
    }

    @Override // javax.inject.Provider
    public ToggleManagerController get() {
        return provideInstance(this.contextProvider, this.broadcastDispatcherProvider, this.bgHandlerProvider);
    }

    public static ToggleManagerController provideInstance(Provider<Context> provider, Provider<BroadcastDispatcher> provider2, Provider<Handler> provider3) {
        return new ToggleManagerController(provider.get(), provider2.get(), provider3.get());
    }

    public static ToggleManagerController_Factory create(Provider<Context> provider, Provider<BroadcastDispatcher> provider2, Provider<Handler> provider3) {
        return new ToggleManagerController_Factory(provider, provider2, provider3);
    }
}
