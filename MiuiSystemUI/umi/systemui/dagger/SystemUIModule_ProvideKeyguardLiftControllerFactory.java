package com.android.systemui.dagger;

import android.content.Context;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.phone.KeyguardLiftController;
import com.android.systemui.util.sensors.AsyncSensorManager;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class SystemUIModule_ProvideKeyguardLiftControllerFactory implements Factory<KeyguardLiftController> {
    private final Provider<AsyncSensorManager> asyncSensorManagerProvider;
    private final Provider<Context> contextProvider;
    private final Provider<DumpManager> dumpManagerProvider;
    private final Provider<KeyguardUpdateMonitor> keyguardUpdateMonitorProvider;
    private final Provider<StatusBarStateController> statusBarStateControllerProvider;

    public SystemUIModule_ProvideKeyguardLiftControllerFactory(Provider<Context> provider, Provider<StatusBarStateController> provider2, Provider<AsyncSensorManager> provider3, Provider<KeyguardUpdateMonitor> provider4, Provider<DumpManager> provider5) {
        this.contextProvider = provider;
        this.statusBarStateControllerProvider = provider2;
        this.asyncSensorManagerProvider = provider3;
        this.keyguardUpdateMonitorProvider = provider4;
        this.dumpManagerProvider = provider5;
    }

    @Override // javax.inject.Provider
    public KeyguardLiftController get() {
        return provideInstance(this.contextProvider, this.statusBarStateControllerProvider, this.asyncSensorManagerProvider, this.keyguardUpdateMonitorProvider, this.dumpManagerProvider);
    }

    public static KeyguardLiftController provideInstance(Provider<Context> provider, Provider<StatusBarStateController> provider2, Provider<AsyncSensorManager> provider3, Provider<KeyguardUpdateMonitor> provider4, Provider<DumpManager> provider5) {
        return proxyProvideKeyguardLiftController(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get());
    }

    public static SystemUIModule_ProvideKeyguardLiftControllerFactory create(Provider<Context> provider, Provider<StatusBarStateController> provider2, Provider<AsyncSensorManager> provider3, Provider<KeyguardUpdateMonitor> provider4, Provider<DumpManager> provider5) {
        return new SystemUIModule_ProvideKeyguardLiftControllerFactory(provider, provider2, provider3, provider4, provider5);
    }

    public static KeyguardLiftController proxyProvideKeyguardLiftController(Context context, StatusBarStateController statusBarStateController, AsyncSensorManager asyncSensorManager, KeyguardUpdateMonitor keyguardUpdateMonitor, DumpManager dumpManager) {
        return SystemUIModule.provideKeyguardLiftController(context, statusBarStateController, asyncSensorManager, keyguardUpdateMonitor, dumpManager);
    }
}
