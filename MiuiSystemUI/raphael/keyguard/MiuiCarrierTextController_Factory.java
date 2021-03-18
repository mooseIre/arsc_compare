package com.android.keyguard;

import android.content.Context;
import android.os.Handler;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiCarrierTextController_Factory implements Factory<MiuiCarrierTextController> {
    private final Provider<Handler> backgroundHandlerProvider;
    private final Provider<Context> contextProvider;
    private final Provider<Handler> mainHandlerProvider;

    public MiuiCarrierTextController_Factory(Provider<Context> provider, Provider<Handler> provider2, Provider<Handler> provider3) {
        this.contextProvider = provider;
        this.mainHandlerProvider = provider2;
        this.backgroundHandlerProvider = provider3;
    }

    @Override // javax.inject.Provider
    public MiuiCarrierTextController get() {
        return provideInstance(this.contextProvider, this.mainHandlerProvider, this.backgroundHandlerProvider);
    }

    public static MiuiCarrierTextController provideInstance(Provider<Context> provider, Provider<Handler> provider2, Provider<Handler> provider3) {
        return new MiuiCarrierTextController(provider.get(), provider2.get(), provider3.get());
    }

    public static MiuiCarrierTextController_Factory create(Provider<Context> provider, Provider<Handler> provider2, Provider<Handler> provider3) {
        return new MiuiCarrierTextController_Factory(provider, provider2, provider3);
    }
}
