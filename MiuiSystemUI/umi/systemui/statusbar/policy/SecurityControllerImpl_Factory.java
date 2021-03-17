package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.os.Handler;
import com.android.systemui.broadcast.BroadcastDispatcher;
import dagger.internal.Factory;
import java.util.concurrent.Executor;
import javax.inject.Provider;

public final class SecurityControllerImpl_Factory implements Factory<SecurityControllerImpl> {
    private final Provider<Executor> bgExecutorProvider;
    private final Provider<Handler> bgHandlerProvider;
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<Context> contextProvider;

    public SecurityControllerImpl_Factory(Provider<Context> provider, Provider<Handler> provider2, Provider<BroadcastDispatcher> provider3, Provider<Executor> provider4) {
        this.contextProvider = provider;
        this.bgHandlerProvider = provider2;
        this.broadcastDispatcherProvider = provider3;
        this.bgExecutorProvider = provider4;
    }

    @Override // javax.inject.Provider
    public SecurityControllerImpl get() {
        return provideInstance(this.contextProvider, this.bgHandlerProvider, this.broadcastDispatcherProvider, this.bgExecutorProvider);
    }

    public static SecurityControllerImpl provideInstance(Provider<Context> provider, Provider<Handler> provider2, Provider<BroadcastDispatcher> provider3, Provider<Executor> provider4) {
        return new SecurityControllerImpl(provider.get(), provider2.get(), provider3.get(), provider4.get());
    }

    public static SecurityControllerImpl_Factory create(Provider<Context> provider, Provider<Handler> provider2, Provider<BroadcastDispatcher> provider3, Provider<Executor> provider4) {
        return new SecurityControllerImpl_Factory(provider, provider2, provider3, provider4);
    }
}
