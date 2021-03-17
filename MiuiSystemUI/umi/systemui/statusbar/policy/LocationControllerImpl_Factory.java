package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.os.Looper;
import com.android.systemui.BootCompleteCache;
import com.android.systemui.broadcast.BroadcastDispatcher;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class LocationControllerImpl_Factory implements Factory<LocationControllerImpl> {
    private final Provider<Looper> bgLooperProvider;
    private final Provider<BootCompleteCache> bootCompleteCacheProvider;
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<Context> contextProvider;
    private final Provider<Looper> mainLooperProvider;

    public LocationControllerImpl_Factory(Provider<Context> provider, Provider<Looper> provider2, Provider<Looper> provider3, Provider<BroadcastDispatcher> provider4, Provider<BootCompleteCache> provider5) {
        this.contextProvider = provider;
        this.mainLooperProvider = provider2;
        this.bgLooperProvider = provider3;
        this.broadcastDispatcherProvider = provider4;
        this.bootCompleteCacheProvider = provider5;
    }

    @Override // javax.inject.Provider
    public LocationControllerImpl get() {
        return provideInstance(this.contextProvider, this.mainLooperProvider, this.bgLooperProvider, this.broadcastDispatcherProvider, this.bootCompleteCacheProvider);
    }

    public static LocationControllerImpl provideInstance(Provider<Context> provider, Provider<Looper> provider2, Provider<Looper> provider3, Provider<BroadcastDispatcher> provider4, Provider<BootCompleteCache> provider5) {
        return new LocationControllerImpl(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get());
    }

    public static LocationControllerImpl_Factory create(Provider<Context> provider, Provider<Looper> provider2, Provider<Looper> provider3, Provider<BroadcastDispatcher> provider4, Provider<BootCompleteCache> provider5) {
        return new LocationControllerImpl_Factory(provider, provider2, provider3, provider4, provider5);
    }
}
