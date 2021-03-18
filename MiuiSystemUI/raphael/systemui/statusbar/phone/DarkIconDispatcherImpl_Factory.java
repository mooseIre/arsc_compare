package com.android.systemui.statusbar.phone;

import android.content.Context;
import com.android.systemui.statusbar.CommandQueue;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class DarkIconDispatcherImpl_Factory implements Factory<DarkIconDispatcherImpl> {
    private final Provider<CommandQueue> commandQueueProvider;
    private final Provider<Context> contextProvider;

    public DarkIconDispatcherImpl_Factory(Provider<Context> provider, Provider<CommandQueue> provider2) {
        this.contextProvider = provider;
        this.commandQueueProvider = provider2;
    }

    @Override // javax.inject.Provider
    public DarkIconDispatcherImpl get() {
        return provideInstance(this.contextProvider, this.commandQueueProvider);
    }

    public static DarkIconDispatcherImpl provideInstance(Provider<Context> provider, Provider<CommandQueue> provider2) {
        return new DarkIconDispatcherImpl(provider.get(), provider2.get());
    }

    public static DarkIconDispatcherImpl_Factory create(Provider<Context> provider, Provider<CommandQueue> provider2) {
        return new DarkIconDispatcherImpl_Factory(provider, provider2);
    }
}
