package com.android.systemui.recents;

import android.content.Context;
import com.android.systemui.statusbar.CommandQueue;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiRecentProxy_Factory implements Factory<MiuiRecentProxy> {
    private final Provider<CommandQueue> commandQueueProvider;
    private final Provider<Context> contextProvider;

    public MiuiRecentProxy_Factory(Provider<Context> provider, Provider<CommandQueue> provider2) {
        this.contextProvider = provider;
        this.commandQueueProvider = provider2;
    }

    @Override // javax.inject.Provider
    public MiuiRecentProxy get() {
        return provideInstance(this.contextProvider, this.commandQueueProvider);
    }

    public static MiuiRecentProxy provideInstance(Provider<Context> provider, Provider<CommandQueue> provider2) {
        return new MiuiRecentProxy(provider.get(), provider2.get());
    }

    public static MiuiRecentProxy_Factory create(Provider<Context> provider, Provider<CommandQueue> provider2) {
        return new MiuiRecentProxy_Factory(provider, provider2);
    }
}
