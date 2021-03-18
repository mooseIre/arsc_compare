package com.android.systemui.statusbar.phone;

import android.content.Context;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import dagger.internal.Factory;
import java.util.concurrent.Executor;
import javax.inject.Provider;

public final class NavigationModeController_Factory implements Factory<NavigationModeController> {
    private final Provider<ConfigurationController> configurationControllerProvider;
    private final Provider<Context> contextProvider;
    private final Provider<DeviceProvisionedController> deviceProvisionedControllerProvider;
    private final Provider<Executor> uiBgExecutorProvider;

    public NavigationModeController_Factory(Provider<Context> provider, Provider<DeviceProvisionedController> provider2, Provider<ConfigurationController> provider3, Provider<Executor> provider4) {
        this.contextProvider = provider;
        this.deviceProvisionedControllerProvider = provider2;
        this.configurationControllerProvider = provider3;
        this.uiBgExecutorProvider = provider4;
    }

    @Override // javax.inject.Provider
    public NavigationModeController get() {
        return provideInstance(this.contextProvider, this.deviceProvisionedControllerProvider, this.configurationControllerProvider, this.uiBgExecutorProvider);
    }

    public static NavigationModeController provideInstance(Provider<Context> provider, Provider<DeviceProvisionedController> provider2, Provider<ConfigurationController> provider3, Provider<Executor> provider4) {
        return new NavigationModeController(provider.get(), provider2.get(), provider3.get(), provider4.get());
    }

    public static NavigationModeController_Factory create(Provider<Context> provider, Provider<DeviceProvisionedController> provider2, Provider<ConfigurationController> provider3, Provider<Executor> provider4) {
        return new NavigationModeController_Factory(provider, provider2, provider3, provider4);
    }
}
