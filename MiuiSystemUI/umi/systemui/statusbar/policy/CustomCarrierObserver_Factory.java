package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.os.Handler;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class CustomCarrierObserver_Factory implements Factory<CustomCarrierObserver> {
    private final Provider<Handler> bgHandlerProvider;
    private final Provider<Context> contextProvider;
    private final Provider<Handler> mainHandlerProvider;

    public CustomCarrierObserver_Factory(Provider<Context> provider, Provider<Handler> provider2, Provider<Handler> provider3) {
        this.contextProvider = provider;
        this.mainHandlerProvider = provider2;
        this.bgHandlerProvider = provider3;
    }

    @Override // javax.inject.Provider
    public CustomCarrierObserver get() {
        return provideInstance(this.contextProvider, this.mainHandlerProvider, this.bgHandlerProvider);
    }

    public static CustomCarrierObserver provideInstance(Provider<Context> provider, Provider<Handler> provider2, Provider<Handler> provider3) {
        return new CustomCarrierObserver(provider.get(), provider2.get(), provider3.get());
    }

    public static CustomCarrierObserver_Factory create(Provider<Context> provider, Provider<Handler> provider2, Provider<Handler> provider3) {
        return new CustomCarrierObserver_Factory(provider, provider2, provider3);
    }
}
