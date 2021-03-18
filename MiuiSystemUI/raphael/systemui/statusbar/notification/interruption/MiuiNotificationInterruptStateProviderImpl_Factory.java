package com.android.systemui.statusbar.notification.interruption;

import android.content.ContentResolver;
import android.content.Context;
import android.hardware.display.AmbientDisplayConfiguration;
import android.os.Handler;
import android.os.PowerManager;
import android.service.dreams.IDreamManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.notification.NotificationFilter;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.miui.systemui.SettingsManager;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiNotificationInterruptStateProviderImpl_Factory implements Factory<MiuiNotificationInterruptStateProviderImpl> {
    private final Provider<AmbientDisplayConfiguration> ambientDisplayConfigurationProvider;
    private final Provider<BatteryController> batteryControllerProvider;
    private final Provider<CommandQueue> commandQueueProvider;
    private final Provider<ContentResolver> contentResolverProvider;
    private final Provider<Context> ctxProvider;
    private final Provider<DeviceProvisionedController> deviceProvisionedControllerProvider;
    private final Provider<IDreamManager> dreamManagerProvider;
    private final Provider<HeadsUpManager> headsUpManagerProvider;
    private final Provider<Handler> mainHandlerProvider;
    private final Provider<NotificationFilter> notificationFilterProvider;
    private final Provider<PowerManager> powerManagerProvider;
    private final Provider<SettingsManager> settingsManagerProvider;
    private final Provider<StatusBarKeyguardViewManager> statusBarKeyguardViewManagerProvider;
    private final Provider<StatusBarStateController> statusBarStateControllerProvider;
    private final Provider<ZenModeController> zenModeControllerProvider;

    public MiuiNotificationInterruptStateProviderImpl_Factory(Provider<Context> provider, Provider<ContentResolver> provider2, Provider<PowerManager> provider3, Provider<IDreamManager> provider4, Provider<AmbientDisplayConfiguration> provider5, Provider<NotificationFilter> provider6, Provider<BatteryController> provider7, Provider<StatusBarStateController> provider8, Provider<HeadsUpManager> provider9, Provider<Handler> provider10, Provider<ZenModeController> provider11, Provider<SettingsManager> provider12, Provider<CommandQueue> provider13, Provider<StatusBarKeyguardViewManager> provider14, Provider<DeviceProvisionedController> provider15) {
        this.ctxProvider = provider;
        this.contentResolverProvider = provider2;
        this.powerManagerProvider = provider3;
        this.dreamManagerProvider = provider4;
        this.ambientDisplayConfigurationProvider = provider5;
        this.notificationFilterProvider = provider6;
        this.batteryControllerProvider = provider7;
        this.statusBarStateControllerProvider = provider8;
        this.headsUpManagerProvider = provider9;
        this.mainHandlerProvider = provider10;
        this.zenModeControllerProvider = provider11;
        this.settingsManagerProvider = provider12;
        this.commandQueueProvider = provider13;
        this.statusBarKeyguardViewManagerProvider = provider14;
        this.deviceProvisionedControllerProvider = provider15;
    }

    @Override // javax.inject.Provider
    public MiuiNotificationInterruptStateProviderImpl get() {
        return provideInstance(this.ctxProvider, this.contentResolverProvider, this.powerManagerProvider, this.dreamManagerProvider, this.ambientDisplayConfigurationProvider, this.notificationFilterProvider, this.batteryControllerProvider, this.statusBarStateControllerProvider, this.headsUpManagerProvider, this.mainHandlerProvider, this.zenModeControllerProvider, this.settingsManagerProvider, this.commandQueueProvider, this.statusBarKeyguardViewManagerProvider, this.deviceProvisionedControllerProvider);
    }

    public static MiuiNotificationInterruptStateProviderImpl provideInstance(Provider<Context> provider, Provider<ContentResolver> provider2, Provider<PowerManager> provider3, Provider<IDreamManager> provider4, Provider<AmbientDisplayConfiguration> provider5, Provider<NotificationFilter> provider6, Provider<BatteryController> provider7, Provider<StatusBarStateController> provider8, Provider<HeadsUpManager> provider9, Provider<Handler> provider10, Provider<ZenModeController> provider11, Provider<SettingsManager> provider12, Provider<CommandQueue> provider13, Provider<StatusBarKeyguardViewManager> provider14, Provider<DeviceProvisionedController> provider15) {
        return new MiuiNotificationInterruptStateProviderImpl(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), provider9.get(), provider10.get(), provider11.get(), provider12.get(), provider13.get(), provider14.get(), provider15.get());
    }

    public static MiuiNotificationInterruptStateProviderImpl_Factory create(Provider<Context> provider, Provider<ContentResolver> provider2, Provider<PowerManager> provider3, Provider<IDreamManager> provider4, Provider<AmbientDisplayConfiguration> provider5, Provider<NotificationFilter> provider6, Provider<BatteryController> provider7, Provider<StatusBarStateController> provider8, Provider<HeadsUpManager> provider9, Provider<Handler> provider10, Provider<ZenModeController> provider11, Provider<SettingsManager> provider12, Provider<CommandQueue> provider13, Provider<StatusBarKeyguardViewManager> provider14, Provider<DeviceProvisionedController> provider15) {
        return new MiuiNotificationInterruptStateProviderImpl_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10, provider11, provider12, provider13, provider14, provider15);
    }
}
