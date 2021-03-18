package com.android.systemui.statusbar.phone;

import android.content.res.Resources;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.dock.DockManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.KeyguardIndicationController;
import com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator;
import com.android.systemui.statusbar.policy.AccessibilityController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class LockscreenLockIconController_Factory implements Factory<LockscreenLockIconController> {
    private final Provider<AccessibilityController> accessibilityControllerProvider;
    private final Provider<ConfigurationController> configurationControllerProvider;
    private final Provider<DockManager> dockManagerProvider;
    private final Provider<HeadsUpManagerPhone> headsUpManagerPhoneProvider;
    private final Provider<KeyguardBypassController> keyguardBypassControllerProvider;
    private final Provider<KeyguardIndicationController> keyguardIndicationControllerProvider;
    private final Provider<KeyguardStateController> keyguardStateControllerProvider;
    private final Provider<KeyguardUpdateMonitor> keyguardUpdateMonitorProvider;
    private final Provider<LockPatternUtils> lockPatternUtilsProvider;
    private final Provider<LockscreenGestureLogger> lockscreenGestureLoggerProvider;
    private final Provider<NotificationWakeUpCoordinator> notificationWakeUpCoordinatorProvider;
    private final Provider<Resources> resourcesProvider;
    private final Provider<ShadeController> shadeControllerProvider;
    private final Provider<StatusBarStateController> statusBarStateControllerProvider;

    public LockscreenLockIconController_Factory(Provider<LockscreenGestureLogger> provider, Provider<KeyguardUpdateMonitor> provider2, Provider<LockPatternUtils> provider3, Provider<ShadeController> provider4, Provider<AccessibilityController> provider5, Provider<KeyguardIndicationController> provider6, Provider<StatusBarStateController> provider7, Provider<ConfigurationController> provider8, Provider<NotificationWakeUpCoordinator> provider9, Provider<KeyguardBypassController> provider10, Provider<DockManager> provider11, Provider<KeyguardStateController> provider12, Provider<Resources> provider13, Provider<HeadsUpManagerPhone> provider14) {
        this.lockscreenGestureLoggerProvider = provider;
        this.keyguardUpdateMonitorProvider = provider2;
        this.lockPatternUtilsProvider = provider3;
        this.shadeControllerProvider = provider4;
        this.accessibilityControllerProvider = provider5;
        this.keyguardIndicationControllerProvider = provider6;
        this.statusBarStateControllerProvider = provider7;
        this.configurationControllerProvider = provider8;
        this.notificationWakeUpCoordinatorProvider = provider9;
        this.keyguardBypassControllerProvider = provider10;
        this.dockManagerProvider = provider11;
        this.keyguardStateControllerProvider = provider12;
        this.resourcesProvider = provider13;
        this.headsUpManagerPhoneProvider = provider14;
    }

    @Override // javax.inject.Provider
    public LockscreenLockIconController get() {
        return provideInstance(this.lockscreenGestureLoggerProvider, this.keyguardUpdateMonitorProvider, this.lockPatternUtilsProvider, this.shadeControllerProvider, this.accessibilityControllerProvider, this.keyguardIndicationControllerProvider, this.statusBarStateControllerProvider, this.configurationControllerProvider, this.notificationWakeUpCoordinatorProvider, this.keyguardBypassControllerProvider, this.dockManagerProvider, this.keyguardStateControllerProvider, this.resourcesProvider, this.headsUpManagerPhoneProvider);
    }

    public static LockscreenLockIconController provideInstance(Provider<LockscreenGestureLogger> provider, Provider<KeyguardUpdateMonitor> provider2, Provider<LockPatternUtils> provider3, Provider<ShadeController> provider4, Provider<AccessibilityController> provider5, Provider<KeyguardIndicationController> provider6, Provider<StatusBarStateController> provider7, Provider<ConfigurationController> provider8, Provider<NotificationWakeUpCoordinator> provider9, Provider<KeyguardBypassController> provider10, Provider<DockManager> provider11, Provider<KeyguardStateController> provider12, Provider<Resources> provider13, Provider<HeadsUpManagerPhone> provider14) {
        return new LockscreenLockIconController(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), provider9.get(), provider10.get(), provider11.get(), provider12.get(), provider13.get(), provider14.get());
    }

    public static LockscreenLockIconController_Factory create(Provider<LockscreenGestureLogger> provider, Provider<KeyguardUpdateMonitor> provider2, Provider<LockPatternUtils> provider3, Provider<ShadeController> provider4, Provider<AccessibilityController> provider5, Provider<KeyguardIndicationController> provider6, Provider<StatusBarStateController> provider7, Provider<ConfigurationController> provider8, Provider<NotificationWakeUpCoordinator> provider9, Provider<KeyguardBypassController> provider10, Provider<DockManager> provider11, Provider<KeyguardStateController> provider12, Provider<Resources> provider13, Provider<HeadsUpManagerPhone> provider14) {
        return new LockscreenLockIconController_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10, provider11, provider12, provider13, provider14);
    }
}
