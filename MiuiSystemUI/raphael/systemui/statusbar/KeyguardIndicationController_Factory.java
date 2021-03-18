package com.android.systemui.statusbar;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.UserManager;
import com.android.internal.app.IBatteryStats;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.dock.DockManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.util.wakelock.WakeLock;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class KeyguardIndicationController_Factory implements Factory<KeyguardIndicationController> {
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<Context> contextProvider;
    private final Provider<DevicePolicyManager> devicePolicyManagerProvider;
    private final Provider<DockManager> dockManagerProvider;
    private final Provider<IBatteryStats> iBatteryStatsProvider;
    private final Provider<KeyguardStateController> keyguardStateControllerProvider;
    private final Provider<KeyguardUpdateMonitor> keyguardUpdateMonitorProvider;
    private final Provider<StatusBarStateController> statusBarStateControllerProvider;
    private final Provider<UserManager> userManagerProvider;
    private final Provider<WakeLock.Builder> wakeLockBuilderProvider;

    public KeyguardIndicationController_Factory(Provider<Context> provider, Provider<WakeLock.Builder> provider2, Provider<KeyguardStateController> provider3, Provider<StatusBarStateController> provider4, Provider<KeyguardUpdateMonitor> provider5, Provider<DockManager> provider6, Provider<BroadcastDispatcher> provider7, Provider<DevicePolicyManager> provider8, Provider<IBatteryStats> provider9, Provider<UserManager> provider10) {
        this.contextProvider = provider;
        this.wakeLockBuilderProvider = provider2;
        this.keyguardStateControllerProvider = provider3;
        this.statusBarStateControllerProvider = provider4;
        this.keyguardUpdateMonitorProvider = provider5;
        this.dockManagerProvider = provider6;
        this.broadcastDispatcherProvider = provider7;
        this.devicePolicyManagerProvider = provider8;
        this.iBatteryStatsProvider = provider9;
        this.userManagerProvider = provider10;
    }

    @Override // javax.inject.Provider
    public KeyguardIndicationController get() {
        return provideInstance(this.contextProvider, this.wakeLockBuilderProvider, this.keyguardStateControllerProvider, this.statusBarStateControllerProvider, this.keyguardUpdateMonitorProvider, this.dockManagerProvider, this.broadcastDispatcherProvider, this.devicePolicyManagerProvider, this.iBatteryStatsProvider, this.userManagerProvider);
    }

    public static KeyguardIndicationController provideInstance(Provider<Context> provider, Provider<WakeLock.Builder> provider2, Provider<KeyguardStateController> provider3, Provider<StatusBarStateController> provider4, Provider<KeyguardUpdateMonitor> provider5, Provider<DockManager> provider6, Provider<BroadcastDispatcher> provider7, Provider<DevicePolicyManager> provider8, Provider<IBatteryStats> provider9, Provider<UserManager> provider10) {
        return new KeyguardIndicationController(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), provider9.get(), provider10.get());
    }

    public static KeyguardIndicationController_Factory create(Provider<Context> provider, Provider<WakeLock.Builder> provider2, Provider<KeyguardStateController> provider3, Provider<StatusBarStateController> provider4, Provider<KeyguardUpdateMonitor> provider5, Provider<DockManager> provider6, Provider<BroadcastDispatcher> provider7, Provider<DevicePolicyManager> provider8, Provider<IBatteryStats> provider9, Provider<UserManager> provider10) {
        return new KeyguardIndicationController_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10);
    }
}
