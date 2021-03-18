package com.android.systemui.util.sensors;

import android.content.res.Resources;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ProximitySensor_Factory implements Factory<ProximitySensor> {
    private final Provider<Resources> resourcesProvider;
    private final Provider<AsyncSensorManager> sensorManagerProvider;

    public ProximitySensor_Factory(Provider<Resources> provider, Provider<AsyncSensorManager> provider2) {
        this.resourcesProvider = provider;
        this.sensorManagerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public ProximitySensor get() {
        return provideInstance(this.resourcesProvider, this.sensorManagerProvider);
    }

    public static ProximitySensor provideInstance(Provider<Resources> provider, Provider<AsyncSensorManager> provider2) {
        return new ProximitySensor(provider.get(), provider2.get());
    }

    public static ProximitySensor_Factory create(Provider<Resources> provider, Provider<AsyncSensorManager> provider2) {
        return new ProximitySensor_Factory(provider, provider2);
    }
}
