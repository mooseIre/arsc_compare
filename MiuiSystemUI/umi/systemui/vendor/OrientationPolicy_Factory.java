package com.android.systemui.vendor;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class OrientationPolicy_Factory implements Factory<OrientationPolicy> {
    private final Provider<Context> contextProvider;

    public OrientationPolicy_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public OrientationPolicy get() {
        return provideInstance(this.contextProvider);
    }

    public static OrientationPolicy provideInstance(Provider<Context> provider) {
        return new OrientationPolicy(provider.get());
    }

    public static OrientationPolicy_Factory create(Provider<Context> provider) {
        return new OrientationPolicy_Factory(provider);
    }
}
