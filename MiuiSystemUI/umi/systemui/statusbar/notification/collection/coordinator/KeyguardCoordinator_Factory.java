package com.android.systemui.statusbar.notification.collection.coordinator;

import android.content.Context;
import android.os.Handler;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.notification.collection.provider.HighPriorityProvider;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class KeyguardCoordinator_Factory implements Factory<KeyguardCoordinator> {
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<Context> contextProvider;
    private final Provider<HighPriorityProvider> highPriorityProvider;
    private final Provider<KeyguardStateController> keyguardStateControllerProvider;
    private final Provider<KeyguardUpdateMonitor> keyguardUpdateMonitorProvider;
    private final Provider<NotificationLockscreenUserManager> lockscreenUserManagerProvider;
    private final Provider<Handler> mainThreadHandlerProvider;
    private final Provider<StatusBarStateController> statusBarStateControllerProvider;

    public KeyguardCoordinator_Factory(Provider<Context> provider, Provider<Handler> provider2, Provider<KeyguardStateController> provider3, Provider<NotificationLockscreenUserManager> provider4, Provider<BroadcastDispatcher> provider5, Provider<StatusBarStateController> provider6, Provider<KeyguardUpdateMonitor> provider7, Provider<HighPriorityProvider> provider8) {
        this.contextProvider = provider;
        this.mainThreadHandlerProvider = provider2;
        this.keyguardStateControllerProvider = provider3;
        this.lockscreenUserManagerProvider = provider4;
        this.broadcastDispatcherProvider = provider5;
        this.statusBarStateControllerProvider = provider6;
        this.keyguardUpdateMonitorProvider = provider7;
        this.highPriorityProvider = provider8;
    }

    @Override // javax.inject.Provider
    public KeyguardCoordinator get() {
        return provideInstance(this.contextProvider, this.mainThreadHandlerProvider, this.keyguardStateControllerProvider, this.lockscreenUserManagerProvider, this.broadcastDispatcherProvider, this.statusBarStateControllerProvider, this.keyguardUpdateMonitorProvider, this.highPriorityProvider);
    }

    public static KeyguardCoordinator provideInstance(Provider<Context> provider, Provider<Handler> provider2, Provider<KeyguardStateController> provider3, Provider<NotificationLockscreenUserManager> provider4, Provider<BroadcastDispatcher> provider5, Provider<StatusBarStateController> provider6, Provider<KeyguardUpdateMonitor> provider7, Provider<HighPriorityProvider> provider8) {
        return new KeyguardCoordinator(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get());
    }

    public static KeyguardCoordinator_Factory create(Provider<Context> provider, Provider<Handler> provider2, Provider<KeyguardStateController> provider3, Provider<NotificationLockscreenUserManager> provider4, Provider<BroadcastDispatcher> provider5, Provider<StatusBarStateController> provider6, Provider<KeyguardUpdateMonitor> provider7, Provider<HighPriorityProvider> provider8) {
        return new KeyguardCoordinator_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8);
    }
}
