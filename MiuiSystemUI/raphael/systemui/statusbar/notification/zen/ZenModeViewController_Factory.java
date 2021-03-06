package com.android.systemui.statusbar.notification.zen;

import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.notification.row.dagger.NotificationRowComponent;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.statusbar.policy.ZenModeController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ZenModeViewController_Factory implements Factory<ZenModeViewController> {
    private final Provider<NotificationRowComponent.Builder> builderProvider;
    private final Provider<KeyguardBypassController> bypassControllerProvider;
    private final Provider<NotificationLockscreenUserManager> notifLockscreenUserManagerProvider;
    private final Provider<SysuiStatusBarStateController> statusBarStateControllerProvider;
    private final Provider<ZenModeController> zenModeControllerProvider;

    public ZenModeViewController_Factory(Provider<ZenModeController> provider, Provider<KeyguardBypassController> provider2, Provider<SysuiStatusBarStateController> provider3, Provider<NotificationLockscreenUserManager> provider4, Provider<NotificationRowComponent.Builder> provider5) {
        this.zenModeControllerProvider = provider;
        this.bypassControllerProvider = provider2;
        this.statusBarStateControllerProvider = provider3;
        this.notifLockscreenUserManagerProvider = provider4;
        this.builderProvider = provider5;
    }

    @Override // javax.inject.Provider
    public ZenModeViewController get() {
        return provideInstance(this.zenModeControllerProvider, this.bypassControllerProvider, this.statusBarStateControllerProvider, this.notifLockscreenUserManagerProvider, this.builderProvider);
    }

    public static ZenModeViewController provideInstance(Provider<ZenModeController> provider, Provider<KeyguardBypassController> provider2, Provider<SysuiStatusBarStateController> provider3, Provider<NotificationLockscreenUserManager> provider4, Provider<NotificationRowComponent.Builder> provider5) {
        return new ZenModeViewController(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get());
    }

    public static ZenModeViewController_Factory create(Provider<ZenModeController> provider, Provider<KeyguardBypassController> provider2, Provider<SysuiStatusBarStateController> provider3, Provider<NotificationLockscreenUserManager> provider4, Provider<NotificationRowComponent.Builder> provider5) {
        return new ZenModeViewController_Factory(provider, provider2, provider3, provider4, provider5);
    }
}
