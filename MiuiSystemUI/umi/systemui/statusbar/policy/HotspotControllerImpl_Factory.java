package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.os.Handler;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class HotspotControllerImpl_Factory implements Factory<HotspotControllerImpl> {
    private final Provider<Handler> backgroundHandlerProvider;
    private final Provider<Context> contextProvider;
    private final Provider<Handler> mainHandlerProvider;

    public HotspotControllerImpl_Factory(Provider<Context> provider, Provider<Handler> provider2, Provider<Handler> provider3) {
        this.contextProvider = provider;
        this.mainHandlerProvider = provider2;
        this.backgroundHandlerProvider = provider3;
    }

    @Override // javax.inject.Provider
    public HotspotControllerImpl get() {
        return provideInstance(this.contextProvider, this.mainHandlerProvider, this.backgroundHandlerProvider);
    }

    public static HotspotControllerImpl provideInstance(Provider<Context> provider, Provider<Handler> provider2, Provider<Handler> provider3) {
        return new HotspotControllerImpl(provider.get(), provider2.get(), provider3.get());
    }

    public static HotspotControllerImpl_Factory create(Provider<Context> provider, Provider<Handler> provider2, Provider<Handler> provider3) {
        return new HotspotControllerImpl_Factory(provider, provider2, provider3);
    }
}
