package com.android.systemui.statusbar;

import android.content.Context;
import android.os.Looper;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NetworkSpeedController_Factory implements Factory<NetworkSpeedController> {
    private final Provider<Looper> bgLooperProvider;
    private final Provider<Context> contextProvider;

    public NetworkSpeedController_Factory(Provider<Context> provider, Provider<Looper> provider2) {
        this.contextProvider = provider;
        this.bgLooperProvider = provider2;
    }

    @Override // javax.inject.Provider
    public NetworkSpeedController get() {
        return provideInstance(this.contextProvider, this.bgLooperProvider);
    }

    public static NetworkSpeedController provideInstance(Provider<Context> provider, Provider<Looper> provider2) {
        return new NetworkSpeedController(provider.get(), provider2.get());
    }

    public static NetworkSpeedController_Factory create(Provider<Context> provider, Provider<Looper> provider2) {
        return new NetworkSpeedController_Factory(provider, provider2);
    }
}
