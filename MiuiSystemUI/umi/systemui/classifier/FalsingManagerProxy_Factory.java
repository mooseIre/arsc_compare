package com.android.systemui.classifier;

import android.content.Context;
import android.util.DisplayMetrics;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.dock.DockManager;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.shared.plugins.PluginManager;
import com.android.systemui.util.DeviceConfigProxy;
import com.android.systemui.util.sensors.ProximitySensor;
import dagger.internal.Factory;
import java.util.concurrent.Executor;
import javax.inject.Provider;

public final class FalsingManagerProxy_Factory implements Factory<FalsingManagerProxy> {
    private final Provider<Context> contextProvider;
    private final Provider<DeviceConfigProxy> deviceConfigProvider;
    private final Provider<DisplayMetrics> displayMetricsProvider;
    private final Provider<DockManager> dockManagerProvider;
    private final Provider<DumpManager> dumpManagerProvider;
    private final Provider<Executor> executorProvider;
    private final Provider<KeyguardUpdateMonitor> keyguardUpdateMonitorProvider;
    private final Provider<PluginManager> pluginManagerProvider;
    private final Provider<ProximitySensor> proximitySensorProvider;
    private final Provider<StatusBarStateController> statusBarStateControllerProvider;
    private final Provider<Executor> uiBgExecutorProvider;

    public FalsingManagerProxy_Factory(Provider<Context> provider, Provider<PluginManager> provider2, Provider<Executor> provider3, Provider<DisplayMetrics> provider4, Provider<ProximitySensor> provider5, Provider<DeviceConfigProxy> provider6, Provider<DockManager> provider7, Provider<KeyguardUpdateMonitor> provider8, Provider<DumpManager> provider9, Provider<Executor> provider10, Provider<StatusBarStateController> provider11) {
        this.contextProvider = provider;
        this.pluginManagerProvider = provider2;
        this.executorProvider = provider3;
        this.displayMetricsProvider = provider4;
        this.proximitySensorProvider = provider5;
        this.deviceConfigProvider = provider6;
        this.dockManagerProvider = provider7;
        this.keyguardUpdateMonitorProvider = provider8;
        this.dumpManagerProvider = provider9;
        this.uiBgExecutorProvider = provider10;
        this.statusBarStateControllerProvider = provider11;
    }

    @Override // javax.inject.Provider
    public FalsingManagerProxy get() {
        return provideInstance(this.contextProvider, this.pluginManagerProvider, this.executorProvider, this.displayMetricsProvider, this.proximitySensorProvider, this.deviceConfigProvider, this.dockManagerProvider, this.keyguardUpdateMonitorProvider, this.dumpManagerProvider, this.uiBgExecutorProvider, this.statusBarStateControllerProvider);
    }

    public static FalsingManagerProxy provideInstance(Provider<Context> provider, Provider<PluginManager> provider2, Provider<Executor> provider3, Provider<DisplayMetrics> provider4, Provider<ProximitySensor> provider5, Provider<DeviceConfigProxy> provider6, Provider<DockManager> provider7, Provider<KeyguardUpdateMonitor> provider8, Provider<DumpManager> provider9, Provider<Executor> provider10, Provider<StatusBarStateController> provider11) {
        return new FalsingManagerProxy(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), provider9.get(), provider10.get(), provider11.get());
    }

    public static FalsingManagerProxy_Factory create(Provider<Context> provider, Provider<PluginManager> provider2, Provider<Executor> provider3, Provider<DisplayMetrics> provider4, Provider<ProximitySensor> provider5, Provider<DeviceConfigProxy> provider6, Provider<DockManager> provider7, Provider<KeyguardUpdateMonitor> provider8, Provider<DumpManager> provider9, Provider<Executor> provider10, Provider<StatusBarStateController> provider11) {
        return new FalsingManagerProxy_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10, provider11);
    }
}
