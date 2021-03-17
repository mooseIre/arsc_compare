package com.android.systemui.statusbar.phone;

import android.content.Context;
import com.android.systemui.statusbar.policy.ConfigurationController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class StatusBarTouchableRegionManager_Factory implements Factory<StatusBarTouchableRegionManager> {
    private final Provider<ConfigurationController> configurationControllerProvider;
    private final Provider<Context> contextProvider;
    private final Provider<HeadsUpManagerPhone> headsUpManagerProvider;
    private final Provider<NotificationShadeWindowController> notificationShadeWindowControllerProvider;

    public StatusBarTouchableRegionManager_Factory(Provider<Context> provider, Provider<NotificationShadeWindowController> provider2, Provider<ConfigurationController> provider3, Provider<HeadsUpManagerPhone> provider4) {
        this.contextProvider = provider;
        this.notificationShadeWindowControllerProvider = provider2;
        this.configurationControllerProvider = provider3;
        this.headsUpManagerProvider = provider4;
    }

    @Override // javax.inject.Provider
    public StatusBarTouchableRegionManager get() {
        return provideInstance(this.contextProvider, this.notificationShadeWindowControllerProvider, this.configurationControllerProvider, this.headsUpManagerProvider);
    }

    public static StatusBarTouchableRegionManager provideInstance(Provider<Context> provider, Provider<NotificationShadeWindowController> provider2, Provider<ConfigurationController> provider3, Provider<HeadsUpManagerPhone> provider4) {
        return new StatusBarTouchableRegionManager(provider.get(), provider2.get(), provider3.get(), provider4.get());
    }

    public static StatusBarTouchableRegionManager_Factory create(Provider<Context> provider, Provider<NotificationShadeWindowController> provider2, Provider<ConfigurationController> provider3, Provider<HeadsUpManagerPhone> provider4) {
        return new StatusBarTouchableRegionManager_Factory(provider, provider2, provider3, provider4);
    }
}
