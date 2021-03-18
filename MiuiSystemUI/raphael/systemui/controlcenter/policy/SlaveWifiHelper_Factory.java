package com.android.systemui.controlcenter.policy;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class SlaveWifiHelper_Factory implements Factory<SlaveWifiHelper> {
    private final Provider<Context> contextProvider;

    public SlaveWifiHelper_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public SlaveWifiHelper get() {
        return provideInstance(this.contextProvider);
    }

    public static SlaveWifiHelper provideInstance(Provider<Context> provider) {
        return new SlaveWifiHelper(provider.get());
    }

    public static SlaveWifiHelper_Factory create(Provider<Context> provider) {
        return new SlaveWifiHelper_Factory(provider);
    }
}
