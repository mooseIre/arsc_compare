package com.android.systemui;

import android.content.Context;
import com.android.systemui.broadcast.BroadcastDispatcher;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class SliceBroadcastRelayHandler_Factory implements Factory<SliceBroadcastRelayHandler> {
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<Context> contextProvider;

    public SliceBroadcastRelayHandler_Factory(Provider<Context> provider, Provider<BroadcastDispatcher> provider2) {
        this.contextProvider = provider;
        this.broadcastDispatcherProvider = provider2;
    }

    @Override // javax.inject.Provider
    public SliceBroadcastRelayHandler get() {
        return provideInstance(this.contextProvider, this.broadcastDispatcherProvider);
    }

    public static SliceBroadcastRelayHandler provideInstance(Provider<Context> provider, Provider<BroadcastDispatcher> provider2) {
        return new SliceBroadcastRelayHandler(provider.get(), provider2.get());
    }

    public static SliceBroadcastRelayHandler_Factory create(Provider<Context> provider, Provider<BroadcastDispatcher> provider2) {
        return new SliceBroadcastRelayHandler_Factory(provider, provider2);
    }
}
