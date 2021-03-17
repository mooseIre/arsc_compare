package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.os.Handler;
import com.android.systemui.broadcast.BroadcastDispatcher;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ZenModeControllerImpl_Factory implements Factory<ZenModeControllerImpl> {
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<Context> contextProvider;
    private final Provider<Handler> handlerProvider;

    public ZenModeControllerImpl_Factory(Provider<Context> provider, Provider<Handler> provider2, Provider<BroadcastDispatcher> provider3) {
        this.contextProvider = provider;
        this.handlerProvider = provider2;
        this.broadcastDispatcherProvider = provider3;
    }

    @Override // javax.inject.Provider
    public ZenModeControllerImpl get() {
        return provideInstance(this.contextProvider, this.handlerProvider, this.broadcastDispatcherProvider);
    }

    public static ZenModeControllerImpl provideInstance(Provider<Context> provider, Provider<Handler> provider2, Provider<BroadcastDispatcher> provider3) {
        return new ZenModeControllerImpl(provider.get(), provider2.get(), provider3.get());
    }

    public static ZenModeControllerImpl_Factory create(Provider<Context> provider, Provider<Handler> provider2, Provider<BroadcastDispatcher> provider3) {
        return new ZenModeControllerImpl_Factory(provider, provider2, provider3);
    }
}
