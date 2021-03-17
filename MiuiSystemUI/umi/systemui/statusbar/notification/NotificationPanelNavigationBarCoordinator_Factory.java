package com.android.systemui.statusbar.notification;

import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.LightBarController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NotificationPanelNavigationBarCoordinator_Factory implements Factory<NotificationPanelNavigationBarCoordinator> {
    private final Provider<CommandQueue> commandQueueProvider;
    private final Provider<ConfigurationController> configurationControllerProvider;
    private final Provider<LightBarController> lightBarControllerProvider;

    public NotificationPanelNavigationBarCoordinator_Factory(Provider<CommandQueue> provider, Provider<ConfigurationController> provider2, Provider<LightBarController> provider3) {
        this.commandQueueProvider = provider;
        this.configurationControllerProvider = provider2;
        this.lightBarControllerProvider = provider3;
    }

    @Override // javax.inject.Provider
    public NotificationPanelNavigationBarCoordinator get() {
        return provideInstance(this.commandQueueProvider, this.configurationControllerProvider, this.lightBarControllerProvider);
    }

    public static NotificationPanelNavigationBarCoordinator provideInstance(Provider<CommandQueue> provider, Provider<ConfigurationController> provider2, Provider<LightBarController> provider3) {
        return new NotificationPanelNavigationBarCoordinator(provider.get(), provider2.get(), provider3.get());
    }

    public static NotificationPanelNavigationBarCoordinator_Factory create(Provider<CommandQueue> provider, Provider<ConfigurationController> provider2, Provider<LightBarController> provider3) {
        return new NotificationPanelNavigationBarCoordinator_Factory(provider, provider2, provider3);
    }
}
