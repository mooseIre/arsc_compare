package com.android.systemui.statusbar;

import android.content.Context;
import com.android.systemui.statusbar.notification.row.dagger.NotificationRowComponent;
import com.android.systemui.statusbar.phone.LockscreenLockIconController;
import com.android.systemui.util.InjectionInflationController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class SuperStatusBarViewFactory_Factory implements Factory<SuperStatusBarViewFactory> {
    private final Provider<Context> contextProvider;
    private final Provider<InjectionInflationController> injectionInflationControllerProvider;
    private final Provider<LockscreenLockIconController> lockIconControllerProvider;
    private final Provider<NotificationRowComponent.Builder> notificationRowComponentBuilderProvider;

    public SuperStatusBarViewFactory_Factory(Provider<Context> provider, Provider<InjectionInflationController> provider2, Provider<NotificationRowComponent.Builder> provider3, Provider<LockscreenLockIconController> provider4) {
        this.contextProvider = provider;
        this.injectionInflationControllerProvider = provider2;
        this.notificationRowComponentBuilderProvider = provider3;
        this.lockIconControllerProvider = provider4;
    }

    @Override // javax.inject.Provider
    public SuperStatusBarViewFactory get() {
        return provideInstance(this.contextProvider, this.injectionInflationControllerProvider, this.notificationRowComponentBuilderProvider, this.lockIconControllerProvider);
    }

    public static SuperStatusBarViewFactory provideInstance(Provider<Context> provider, Provider<InjectionInflationController> provider2, Provider<NotificationRowComponent.Builder> provider3, Provider<LockscreenLockIconController> provider4) {
        return new SuperStatusBarViewFactory(provider.get(), provider2.get(), provider3.get(), provider4.get());
    }

    public static SuperStatusBarViewFactory_Factory create(Provider<Context> provider, Provider<InjectionInflationController> provider2, Provider<NotificationRowComponent.Builder> provider3, Provider<LockscreenLockIconController> provider4) {
        return new SuperStatusBarViewFactory_Factory(provider, provider2, provider3, provider4);
    }
}
