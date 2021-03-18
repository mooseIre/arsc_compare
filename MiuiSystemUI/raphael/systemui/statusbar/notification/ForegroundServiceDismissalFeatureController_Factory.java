package com.android.systemui.statusbar.notification;

import android.content.Context;
import com.android.systemui.util.DeviceConfigProxy;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ForegroundServiceDismissalFeatureController_Factory implements Factory<ForegroundServiceDismissalFeatureController> {
    private final Provider<Context> contextProvider;
    private final Provider<DeviceConfigProxy> proxyProvider;

    public ForegroundServiceDismissalFeatureController_Factory(Provider<DeviceConfigProxy> provider, Provider<Context> provider2) {
        this.proxyProvider = provider;
        this.contextProvider = provider2;
    }

    @Override // javax.inject.Provider
    public ForegroundServiceDismissalFeatureController get() {
        return provideInstance(this.proxyProvider, this.contextProvider);
    }

    public static ForegroundServiceDismissalFeatureController provideInstance(Provider<DeviceConfigProxy> provider, Provider<Context> provider2) {
        return new ForegroundServiceDismissalFeatureController(provider.get(), provider2.get());
    }

    public static ForegroundServiceDismissalFeatureController_Factory create(Provider<DeviceConfigProxy> provider, Provider<Context> provider2) {
        return new ForegroundServiceDismissalFeatureController_Factory(provider, provider2);
    }
}
