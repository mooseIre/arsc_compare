package com.android.systemui.tuner;

import com.android.systemui.tuner.TunablePadding;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class TunablePadding_TunablePaddingService_Factory implements Factory<TunablePadding.TunablePaddingService> {
    private final Provider<TunerService> tunerServiceProvider;

    public TunablePadding_TunablePaddingService_Factory(Provider<TunerService> provider) {
        this.tunerServiceProvider = provider;
    }

    @Override // javax.inject.Provider
    public TunablePadding.TunablePaddingService get() {
        return provideInstance(this.tunerServiceProvider);
    }

    public static TunablePadding.TunablePaddingService provideInstance(Provider<TunerService> provider) {
        return new TunablePadding.TunablePaddingService(provider.get());
    }

    public static TunablePadding_TunablePaddingService_Factory create(Provider<TunerService> provider) {
        return new TunablePadding_TunablePaddingService_Factory(provider);
    }
}
