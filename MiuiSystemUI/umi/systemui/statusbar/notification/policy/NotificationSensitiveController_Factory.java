package com.android.systemui.statusbar.notification.policy;

import android.content.Context;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.faceunlock.MiuiFaceUnlockManager;
import com.android.keyguard.injector.KeyguardUpdateMonitorInjector;
import com.android.systemui.statusbar.NotificationViewHierarchyManager;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NotificationSensitiveController_Factory implements Factory<NotificationSensitiveController> {
    private final Provider<Context> contextProvider;
    private final Provider<KeyguardUpdateMonitorInjector> keyguardUpdateMonitorInjectorProvider;
    private final Provider<KeyguardUpdateMonitor> keyguardUpdateMonitorProvider;
    private final Provider<MiuiFaceUnlockManager> miuiFaceUnlockManagerProvider;
    private final Provider<NotificationViewHierarchyManager> notificationViewHierarchyManagerProvider;
    private final Provider<UserSwitcherController> userSwitcherControllerProvider;

    public NotificationSensitiveController_Factory(Provider<Context> provider, Provider<MiuiFaceUnlockManager> provider2, Provider<KeyguardUpdateMonitor> provider3, Provider<KeyguardUpdateMonitorInjector> provider4, Provider<UserSwitcherController> provider5, Provider<NotificationViewHierarchyManager> provider6) {
        this.contextProvider = provider;
        this.miuiFaceUnlockManagerProvider = provider2;
        this.keyguardUpdateMonitorProvider = provider3;
        this.keyguardUpdateMonitorInjectorProvider = provider4;
        this.userSwitcherControllerProvider = provider5;
        this.notificationViewHierarchyManagerProvider = provider6;
    }

    public NotificationSensitiveController get() {
        return provideInstance(this.contextProvider, this.miuiFaceUnlockManagerProvider, this.keyguardUpdateMonitorProvider, this.keyguardUpdateMonitorInjectorProvider, this.userSwitcherControllerProvider, this.notificationViewHierarchyManagerProvider);
    }

    public static NotificationSensitiveController provideInstance(Provider<Context> provider, Provider<MiuiFaceUnlockManager> provider2, Provider<KeyguardUpdateMonitor> provider3, Provider<KeyguardUpdateMonitorInjector> provider4, Provider<UserSwitcherController> provider5, Provider<NotificationViewHierarchyManager> provider6) {
        return new NotificationSensitiveController(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get());
    }

    public static NotificationSensitiveController_Factory create(Provider<Context> provider, Provider<MiuiFaceUnlockManager> provider2, Provider<KeyguardUpdateMonitor> provider3, Provider<KeyguardUpdateMonitorInjector> provider4, Provider<UserSwitcherController> provider5, Provider<NotificationViewHierarchyManager> provider6) {
        return new NotificationSensitiveController_Factory(provider, provider2, provider3, provider4, provider5, provider6);
    }
}
