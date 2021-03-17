package com.android.systemui.statusbar.notification.policy;

import android.app.INotificationManager;
import android.content.Context;
import com.android.systemui.keyguard.ScreenLifecycle;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.miui.systemui.SettingsManager;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NotificationAlertController_Factory implements Factory<NotificationAlertController> {
    private final Provider<Context> contextProvider;
    private final Provider<NotificationEntryManager> entryManagerProvider;
    private final Provider<NotificationGroupManager> groupManagerProvider;
    private final Provider<INotificationManager> nmProvider;
    private final Provider<NotificationLockscreenUserManager> notificationLockscreenUserManagerProvider;
    private final Provider<ScreenLifecycle> screenLifecycleProvider;
    private final Provider<SettingsManager> settingsManagerProvider;
    private final Provider<StatusBarKeyguardViewManager> statusBarKeyguardViewManagerProvider;
    private final Provider<StatusBarStateController> statusBarStateControllerProvider;
    private final Provider<ZenModeController> zenModeControllerProvider;

    public NotificationAlertController_Factory(Provider<Context> provider, Provider<INotificationManager> provider2, Provider<NotificationEntryManager> provider3, Provider<NotificationGroupManager> provider4, Provider<StatusBarStateController> provider5, Provider<ScreenLifecycle> provider6, Provider<ZenModeController> provider7, Provider<SettingsManager> provider8, Provider<NotificationLockscreenUserManager> provider9, Provider<StatusBarKeyguardViewManager> provider10) {
        this.contextProvider = provider;
        this.nmProvider = provider2;
        this.entryManagerProvider = provider3;
        this.groupManagerProvider = provider4;
        this.statusBarStateControllerProvider = provider5;
        this.screenLifecycleProvider = provider6;
        this.zenModeControllerProvider = provider7;
        this.settingsManagerProvider = provider8;
        this.notificationLockscreenUserManagerProvider = provider9;
        this.statusBarKeyguardViewManagerProvider = provider10;
    }

    @Override // javax.inject.Provider
    public NotificationAlertController get() {
        return provideInstance(this.contextProvider, this.nmProvider, this.entryManagerProvider, this.groupManagerProvider, this.statusBarStateControllerProvider, this.screenLifecycleProvider, this.zenModeControllerProvider, this.settingsManagerProvider, this.notificationLockscreenUserManagerProvider, this.statusBarKeyguardViewManagerProvider);
    }

    public static NotificationAlertController provideInstance(Provider<Context> provider, Provider<INotificationManager> provider2, Provider<NotificationEntryManager> provider3, Provider<NotificationGroupManager> provider4, Provider<StatusBarStateController> provider5, Provider<ScreenLifecycle> provider6, Provider<ZenModeController> provider7, Provider<SettingsManager> provider8, Provider<NotificationLockscreenUserManager> provider9, Provider<StatusBarKeyguardViewManager> provider10) {
        return new NotificationAlertController(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), provider9.get(), provider10.get());
    }

    public static NotificationAlertController_Factory create(Provider<Context> provider, Provider<INotificationManager> provider2, Provider<NotificationEntryManager> provider3, Provider<NotificationGroupManager> provider4, Provider<StatusBarStateController> provider5, Provider<ScreenLifecycle> provider6, Provider<ZenModeController> provider7, Provider<SettingsManager> provider8, Provider<NotificationLockscreenUserManager> provider9, Provider<StatusBarKeyguardViewManager> provider10) {
        return new NotificationAlertController_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10);
    }
}
