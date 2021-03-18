package com.android.systemui;

import android.content.Context;
import android.os.Handler;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.tuner.TunerService;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ScreenDecorations_Factory implements Factory<ScreenDecorations> {
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<CommandQueue> commandQueueProvider;
    private final Provider<Context> contextProvider;
    private final Provider<Handler> handlerProvider;
    private final Provider<TunerService> tunerServiceProvider;

    public ScreenDecorations_Factory(Provider<Context> provider, Provider<Handler> provider2, Provider<BroadcastDispatcher> provider3, Provider<TunerService> provider4, Provider<CommandQueue> provider5) {
        this.contextProvider = provider;
        this.handlerProvider = provider2;
        this.broadcastDispatcherProvider = provider3;
        this.tunerServiceProvider = provider4;
        this.commandQueueProvider = provider5;
    }

    @Override // javax.inject.Provider
    public ScreenDecorations get() {
        return provideInstance(this.contextProvider, this.handlerProvider, this.broadcastDispatcherProvider, this.tunerServiceProvider, this.commandQueueProvider);
    }

    public static ScreenDecorations provideInstance(Provider<Context> provider, Provider<Handler> provider2, Provider<BroadcastDispatcher> provider3, Provider<TunerService> provider4, Provider<CommandQueue> provider5) {
        return new ScreenDecorations(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get());
    }

    public static ScreenDecorations_Factory create(Provider<Context> provider, Provider<Handler> provider2, Provider<BroadcastDispatcher> provider3, Provider<TunerService> provider4, Provider<CommandQueue> provider5) {
        return new ScreenDecorations_Factory(provider, provider2, provider3, provider4, provider5);
    }
}
