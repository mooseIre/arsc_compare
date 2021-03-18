package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.os.Handler;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class CarrierObserver_Factory implements Factory<CarrierObserver> {
    private final Provider<Handler> bgHandlerProvider;
    private final Provider<Context> contextProvider;
    private final Provider<Handler> mainHandlerProvider;

    public CarrierObserver_Factory(Provider<Context> provider, Provider<Handler> provider2, Provider<Handler> provider3) {
        this.contextProvider = provider;
        this.mainHandlerProvider = provider2;
        this.bgHandlerProvider = provider3;
    }

    @Override // javax.inject.Provider
    public CarrierObserver get() {
        return provideInstance(this.contextProvider, this.mainHandlerProvider, this.bgHandlerProvider);
    }

    public static CarrierObserver provideInstance(Provider<Context> provider, Provider<Handler> provider2, Provider<Handler> provider3) {
        return new CarrierObserver(provider.get(), provider2.get(), provider3.get());
    }

    public static CarrierObserver_Factory create(Provider<Context> provider, Provider<Handler> provider2, Provider<Handler> provider3) {
        return new CarrierObserver_Factory(provider, provider2, provider3);
    }
}
