package com.android.systemui.statusbar.phone;

import android.content.Context;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.ViewMediatorCallback;
import com.android.systemui.dock.DockManager;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class StatusBarKeyguardViewManager_Factory implements Factory<StatusBarKeyguardViewManager> {
    private final Provider<ViewMediatorCallback> callbackProvider;
    private final Provider<ConfigurationController> configurationControllerProvider;
    private final Provider<Context> contextProvider;
    private final Provider<DockManager> dockManagerProvider;
    private final Provider<KeyguardStateController> keyguardStateControllerProvider;
    private final Provider<KeyguardUpdateMonitor> keyguardUpdateMonitorProvider;
    private final Provider<LockPatternUtils> lockPatternUtilsProvider;
    private final Provider<NavigationModeController> navigationModeControllerProvider;
    private final Provider<NotificationMediaManager> notificationMediaManagerProvider;
    private final Provider<NotificationShadeWindowController> notificationShadeWindowControllerProvider;
    private final Provider<SysuiStatusBarStateController> sysuiStatusBarStateControllerProvider;

    public StatusBarKeyguardViewManager_Factory(Provider<Context> provider, Provider<ViewMediatorCallback> provider2, Provider<LockPatternUtils> provider3, Provider<SysuiStatusBarStateController> provider4, Provider<ConfigurationController> provider5, Provider<KeyguardUpdateMonitor> provider6, Provider<NavigationModeController> provider7, Provider<DockManager> provider8, Provider<NotificationShadeWindowController> provider9, Provider<KeyguardStateController> provider10, Provider<NotificationMediaManager> provider11) {
        this.contextProvider = provider;
        this.callbackProvider = provider2;
        this.lockPatternUtilsProvider = provider3;
        this.sysuiStatusBarStateControllerProvider = provider4;
        this.configurationControllerProvider = provider5;
        this.keyguardUpdateMonitorProvider = provider6;
        this.navigationModeControllerProvider = provider7;
        this.dockManagerProvider = provider8;
        this.notificationShadeWindowControllerProvider = provider9;
        this.keyguardStateControllerProvider = provider10;
        this.notificationMediaManagerProvider = provider11;
    }

    @Override // javax.inject.Provider
    public StatusBarKeyguardViewManager get() {
        return provideInstance(this.contextProvider, this.callbackProvider, this.lockPatternUtilsProvider, this.sysuiStatusBarStateControllerProvider, this.configurationControllerProvider, this.keyguardUpdateMonitorProvider, this.navigationModeControllerProvider, this.dockManagerProvider, this.notificationShadeWindowControllerProvider, this.keyguardStateControllerProvider, this.notificationMediaManagerProvider);
    }

    public static StatusBarKeyguardViewManager provideInstance(Provider<Context> provider, Provider<ViewMediatorCallback> provider2, Provider<LockPatternUtils> provider3, Provider<SysuiStatusBarStateController> provider4, Provider<ConfigurationController> provider5, Provider<KeyguardUpdateMonitor> provider6, Provider<NavigationModeController> provider7, Provider<DockManager> provider8, Provider<NotificationShadeWindowController> provider9, Provider<KeyguardStateController> provider10, Provider<NotificationMediaManager> provider11) {
        return new StatusBarKeyguardViewManager(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), provider9.get(), provider10.get(), provider11.get());
    }

    public static StatusBarKeyguardViewManager_Factory create(Provider<Context> provider, Provider<ViewMediatorCallback> provider2, Provider<LockPatternUtils> provider3, Provider<SysuiStatusBarStateController> provider4, Provider<ConfigurationController> provider5, Provider<KeyguardUpdateMonitor> provider6, Provider<NavigationModeController> provider7, Provider<DockManager> provider8, Provider<NotificationShadeWindowController> provider9, Provider<KeyguardStateController> provider10, Provider<NotificationMediaManager> provider11) {
        return new StatusBarKeyguardViewManager_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10, provider11);
    }
}
