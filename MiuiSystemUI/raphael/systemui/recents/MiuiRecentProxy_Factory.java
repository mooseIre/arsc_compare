package com.android.systemui.recents;

import android.content.Context;
import android.os.Handler;
import com.android.systemui.statusbar.CommandQueue;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiRecentProxy_Factory implements Factory<MiuiRecentProxy> {
    private final Provider<CommandQueue> commandQueueProvider;
    private final Provider<Context> contextProvider;
    private final Provider<Handler> handlerProvider;

    public MiuiRecentProxy_Factory(Provider<Context> provider, Provider<CommandQueue> provider2, Provider<Handler> provider3) {
        this.contextProvider = provider;
        this.commandQueueProvider = provider2;
        this.handlerProvider = provider3;
    }

    @Override // javax.inject.Provider
    public MiuiRecentProxy get() {
        return provideInstance(this.contextProvider, this.commandQueueProvider, this.handlerProvider);
    }

    public static MiuiRecentProxy provideInstance(Provider<Context> provider, Provider<CommandQueue> provider2, Provider<Handler> provider3) {
        return new MiuiRecentProxy(provider.get(), provider2.get(), provider3.get());
    }

    public static MiuiRecentProxy_Factory create(Provider<Context> provider, Provider<CommandQueue> provider2, Provider<Handler> provider3) {
        return new MiuiRecentProxy_Factory(provider, provider2, provider3);
    }
}
