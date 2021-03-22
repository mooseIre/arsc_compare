package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.os.Handler;
import com.android.systemui.broadcast.BroadcastDispatcher;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NextAlarmControllerImpl_Factory implements Factory<NextAlarmControllerImpl> {
    private final Provider<Handler> bgHandlerProvider;
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<Context> contextProvider;
    private final Provider<Handler> mainHandlerProvider;

    public NextAlarmControllerImpl_Factory(Provider<Context> provider, Provider<BroadcastDispatcher> provider2, Provider<Handler> provider3, Provider<Handler> provider4) {
        this.contextProvider = provider;
        this.broadcastDispatcherProvider = provider2;
        this.mainHandlerProvider = provider3;
        this.bgHandlerProvider = provider4;
    }

    @Override // javax.inject.Provider
    public NextAlarmControllerImpl get() {
        return provideInstance(this.contextProvider, this.broadcastDispatcherProvider, this.mainHandlerProvider, this.bgHandlerProvider);
    }

    public static NextAlarmControllerImpl provideInstance(Provider<Context> provider, Provider<BroadcastDispatcher> provider2, Provider<Handler> provider3, Provider<Handler> provider4) {
        return new NextAlarmControllerImpl(provider.get(), provider2.get(), provider3.get(), provider4.get());
    }

    public static NextAlarmControllerImpl_Factory create(Provider<Context> provider, Provider<BroadcastDispatcher> provider2, Provider<Handler> provider3, Provider<Handler> provider4) {
        return new NextAlarmControllerImpl_Factory(provider, provider2, provider3, provider4);
    }
}
