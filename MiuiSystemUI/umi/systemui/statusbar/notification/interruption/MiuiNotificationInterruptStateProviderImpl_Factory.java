package com.android.systemui.statusbar.notification.interruption;

import android.content.ContentResolver;
import android.hardware.display.AmbientDisplayConfiguration;
import android.os.Handler;
import android.os.PowerManager;
import android.service.dreams.IDreamManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.notification.NotificationFilter;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.android.systemui.statusbar.policy.BatteryController;
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
    private final Provider<IDreamManager> dreamManagerProvider;
    private final Provider<HeadsUpManager> headsUpManagerProvider;
    private final Provider<Handler> mainHandlerProvider;
    private final Provider<NotificationFilter> notificationFilterProvider;
    private final Provider<PowerManager> powerManagerProvider;
    private final Provider<SettingsManager> settingsManagerProvider;
    private final Provider<StatusBarKeyguardViewManager> statusBarKeyguardViewManagerProvider;
    private final Provider<StatusBarStateController> statusBarStateControllerProvider;
    private final Provider<ZenModeController> zenModeControllerProvider;

    public MiuiNotificationInterruptStateProviderImpl_Factory(Provider<ContentResolver> provider, Provider<PowerManager> provider2, Provider<IDreamManager> provider3, Provider<AmbientDisplayConfiguration> provider4, Provider<NotificationFilter> provider5, Provider<BatteryController> provider6, Provider<StatusBarStateController> provider7, Provider<HeadsUpManager> provider8, Provider<Handler> provider9, Provider<ZenModeController> provider10, Provider<SettingsManager> provider11, Provider<CommandQueue> provider12, Provider<StatusBarKeyguardViewManager> provider13) {
        this.contentResolverProvider = provider;
        this.powerManagerProvider = provider2;
        this.dreamManagerProvider = provider3;
        this.ambientDisplayConfigurationProvider = provider4;
        this.notificationFilterProvider = provider5;
        this.batteryControllerProvider = provider6;
        this.statusBarStateControllerProvider = provider7;
        this.headsUpManagerProvider = provider8;
        this.mainHandlerProvider = provider9;
        this.zenModeControllerProvider = provider10;
        this.settingsManagerProvider = provider11;
        this.commandQueueProvider = provider12;
        this.statusBarKeyguardViewManagerProvider = provider13;
    }

    public MiuiNotificationInterruptStateProviderImpl get() {
        return provideInstance(this.contentResolverProvider, this.powerManagerProvider, this.dreamManagerProvider, this.ambientDisplayConfigurationProvider, this.notificationFilterProvider, this.batteryControllerProvider, this.statusBarStateControllerProvider, this.headsUpManagerProvider, this.mainHandlerProvider, this.zenModeControllerProvider, this.settingsManagerProvider, this.commandQueueProvider, this.statusBarKeyguardViewManagerProvider);
    }

    public static MiuiNotificationInterruptStateProviderImpl provideInstance(Provider<ContentResolver> provider, Provider<PowerManager> provider2, Provider<IDreamManager> provider3, Provider<AmbientDisplayConfiguration> provider4, Provider<NotificationFilter> provider5, Provider<BatteryController> provider6, Provider<StatusBarStateController> provider7, Provider<HeadsUpManager> provider8, Provider<Handler> provider9, Provider<ZenModeController> provider10, Provider<SettingsManager> provider11, Provider<CommandQueue> provider12, Provider<StatusBarKeyguardViewManager> provider13) {
        return new MiuiNotificationInterruptStateProviderImpl(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), provider9.get(), provider10.get(), provider11.get(), provider12.get(), provider13.get());
    }

    public static MiuiNotificationInterruptStateProviderImpl_Factory create(Provider<ContentResolver> provider, Provider<PowerManager> provider2, Provider<IDreamManager> provider3, Provider<AmbientDisplayConfiguration> provider4, Provider<NotificationFilter> provider5, Provider<BatteryController> provider6, Provider<StatusBarStateController> provider7, Provider<HeadsUpManager> provider8, Provider<Handler> provider9, Provider<ZenModeController> provider10, Provider<SettingsManager> provider11, Provider<CommandQueue> provider12, Provider<StatusBarKeyguardViewManager> provider13) {
        return new MiuiNotificationInterruptStateProviderImpl_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10, provider11, provider12, provider13);
    }
}
