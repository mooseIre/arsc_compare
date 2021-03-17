package com.android.systemui.vendor;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class HeadsetPolicy_Factory implements Factory<HeadsetPolicy> {
    private final Provider<Context> contextProvider;

    public HeadsetPolicy_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public HeadsetPolicy get() {
        return provideInstance(this.contextProvider);
    }

    public static HeadsetPolicy provideInstance(Provider<Context> provider) {
        return new HeadsetPolicy(provider.get());
    }

    public static HeadsetPolicy_Factory create(Provider<Context> provider) {
        return new HeadsetPolicy_Factory(provider);
    }
}
