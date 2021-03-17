package com.android.systemui.statusbar.notification.collection.coordinator;

import android.content.pm.IPackageManager;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class DeviceProvisionedCoordinator_Factory implements Factory<DeviceProvisionedCoordinator> {
    private final Provider<DeviceProvisionedController> deviceProvisionedControllerProvider;
    private final Provider<IPackageManager> packageManagerProvider;

    public DeviceProvisionedCoordinator_Factory(Provider<DeviceProvisionedController> provider, Provider<IPackageManager> provider2) {
        this.deviceProvisionedControllerProvider = provider;
        this.packageManagerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public DeviceProvisionedCoordinator get() {
        return provideInstance(this.deviceProvisionedControllerProvider, this.packageManagerProvider);
    }

    public static DeviceProvisionedCoordinator provideInstance(Provider<DeviceProvisionedController> provider, Provider<IPackageManager> provider2) {
        return new DeviceProvisionedCoordinator(provider.get(), provider2.get());
    }

    public static DeviceProvisionedCoordinator_Factory create(Provider<DeviceProvisionedController> provider, Provider<IPackageManager> provider2) {
        return new DeviceProvisionedCoordinator_Factory(provider, provider2);
    }
}
