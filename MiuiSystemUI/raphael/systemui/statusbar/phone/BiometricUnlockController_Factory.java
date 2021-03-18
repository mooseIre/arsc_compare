package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import com.android.internal.logging.MetricsLogger;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class BiometricUnlockController_Factory implements Factory<BiometricUnlockController> {
    private final Provider<Context> contextProvider;
    private final Provider<DozeParameters> dozeParametersProvider;
    private final Provider<DozeScrimController> dozeScrimControllerProvider;
    private final Provider<DumpManager> dumpManagerProvider;
    private final Provider<Handler> handlerProvider;
    private final Provider<KeyguardBypassController> keyguardBypassControllerProvider;
    private final Provider<KeyguardStateController> keyguardStateControllerProvider;
    private final Provider<KeyguardUpdateMonitor> keyguardUpdateMonitorProvider;
    private final Provider<KeyguardViewMediator> keyguardViewMediatorProvider;
    private final Provider<MetricsLogger> metricsLoggerProvider;
    private final Provider<NotificationShadeWindowController> notificationShadeWindowControllerProvider;
    private final Provider<Resources> resourcesProvider;
    private final Provider<ScrimController> scrimControllerProvider;
    private final Provider<ShadeController> shadeControllerProvider;
    private final Provider<StatusBar> statusBarProvider;

    public BiometricUnlockController_Factory(Provider<Context> provider, Provider<DozeScrimController> provider2, Provider<KeyguardViewMediator> provider3, Provider<ScrimController> provider4, Provider<StatusBar> provider5, Provider<ShadeController> provider6, Provider<NotificationShadeWindowController> provider7, Provider<KeyguardStateController> provider8, Provider<Handler> provider9, Provider<KeyguardUpdateMonitor> provider10, Provider<Resources> provider11, Provider<KeyguardBypassController> provider12, Provider<DozeParameters> provider13, Provider<MetricsLogger> provider14, Provider<DumpManager> provider15) {
        this.contextProvider = provider;
        this.dozeScrimControllerProvider = provider2;
        this.keyguardViewMediatorProvider = provider3;
        this.scrimControllerProvider = provider4;
        this.statusBarProvider = provider5;
        this.shadeControllerProvider = provider6;
        this.notificationShadeWindowControllerProvider = provider7;
        this.keyguardStateControllerProvider = provider8;
        this.handlerProvider = provider9;
        this.keyguardUpdateMonitorProvider = provider10;
        this.resourcesProvider = provider11;
        this.keyguardBypassControllerProvider = provider12;
        this.dozeParametersProvider = provider13;
        this.metricsLoggerProvider = provider14;
        this.dumpManagerProvider = provider15;
    }

    @Override // javax.inject.Provider
    public BiometricUnlockController get() {
        return provideInstance(this.contextProvider, this.dozeScrimControllerProvider, this.keyguardViewMediatorProvider, this.scrimControllerProvider, this.statusBarProvider, this.shadeControllerProvider, this.notificationShadeWindowControllerProvider, this.keyguardStateControllerProvider, this.handlerProvider, this.keyguardUpdateMonitorProvider, this.resourcesProvider, this.keyguardBypassControllerProvider, this.dozeParametersProvider, this.metricsLoggerProvider, this.dumpManagerProvider);
    }

    public static BiometricUnlockController provideInstance(Provider<Context> provider, Provider<DozeScrimController> provider2, Provider<KeyguardViewMediator> provider3, Provider<ScrimController> provider4, Provider<StatusBar> provider5, Provider<ShadeController> provider6, Provider<NotificationShadeWindowController> provider7, Provider<KeyguardStateController> provider8, Provider<Handler> provider9, Provider<KeyguardUpdateMonitor> provider10, Provider<Resources> provider11, Provider<KeyguardBypassController> provider12, Provider<DozeParameters> provider13, Provider<MetricsLogger> provider14, Provider<DumpManager> provider15) {
        return new BiometricUnlockController(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), provider9.get(), provider10.get(), provider11.get(), provider12.get(), provider13.get(), provider14.get(), provider15.get());
    }

    public static BiometricUnlockController_Factory create(Provider<Context> provider, Provider<DozeScrimController> provider2, Provider<KeyguardViewMediator> provider3, Provider<ScrimController> provider4, Provider<StatusBar> provider5, Provider<ShadeController> provider6, Provider<NotificationShadeWindowController> provider7, Provider<KeyguardStateController> provider8, Provider<Handler> provider9, Provider<KeyguardUpdateMonitor> provider10, Provider<Resources> provider11, Provider<KeyguardBypassController> provider12, Provider<DozeParameters> provider13, Provider<MetricsLogger> provider14, Provider<DumpManager> provider15) {
        return new BiometricUnlockController_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10, provider11, provider12, provider13, provider14, provider15);
    }
}
