package com.android.systemui.statusbar.phone;

import android.content.Context;
import com.android.systemui.broadcast.BroadcastDispatcher;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ManagedProfileControllerImpl_Factory implements Factory<ManagedProfileControllerImpl> {
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<Context> contextProvider;

    public ManagedProfileControllerImpl_Factory(Provider<Context> provider, Provider<BroadcastDispatcher> provider2) {
        this.contextProvider = provider;
        this.broadcastDispatcherProvider = provider2;
    }

    @Override // javax.inject.Provider
    public ManagedProfileControllerImpl get() {
        return provideInstance(this.contextProvider, this.broadcastDispatcherProvider);
    }

    public static ManagedProfileControllerImpl provideInstance(Provider<Context> provider, Provider<BroadcastDispatcher> provider2) {
        return new ManagedProfileControllerImpl(provider.get(), provider2.get());
    }

    public static ManagedProfileControllerImpl_Factory create(Provider<Context> provider, Provider<BroadcastDispatcher> provider2) {
        return new ManagedProfileControllerImpl_Factory(provider, provider2);
    }
}
