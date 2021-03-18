package com.android.systemui.statusbar.phone;

import android.app.AlarmManager;
import android.os.Handler;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.colorextraction.SysuiColorExtractor;
import com.android.systemui.dock.DockManager;
import com.android.systemui.statusbar.BlurUtils;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.util.wakelock.DelayedWakeLock;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ScrimController_Factory implements Factory<ScrimController> {
    private final Provider<AlarmManager> alarmManagerProvider;
    private final Provider<BlurUtils> blurUtilsProvider;
    private final Provider<DelayedWakeLock.Builder> delayedWakeLockBuilderProvider;
    private final Provider<DockManager> dockManagerProvider;
    private final Provider<DozeParameters> dozeParametersProvider;
    private final Provider<Handler> handlerProvider;
    private final Provider<KeyguardStateController> keyguardStateControllerProvider;
    private final Provider<KeyguardUpdateMonitor> keyguardUpdateMonitorProvider;
    private final Provider<LightBarController> lightBarControllerProvider;
    private final Provider<SysuiColorExtractor> sysuiColorExtractorProvider;

    public ScrimController_Factory(Provider<LightBarController> provider, Provider<DozeParameters> provider2, Provider<AlarmManager> provider3, Provider<KeyguardStateController> provider4, Provider<DelayedWakeLock.Builder> provider5, Provider<Handler> provider6, Provider<KeyguardUpdateMonitor> provider7, Provider<SysuiColorExtractor> provider8, Provider<DockManager> provider9, Provider<BlurUtils> provider10) {
        this.lightBarControllerProvider = provider;
        this.dozeParametersProvider = provider2;
        this.alarmManagerProvider = provider3;
        this.keyguardStateControllerProvider = provider4;
        this.delayedWakeLockBuilderProvider = provider5;
        this.handlerProvider = provider6;
        this.keyguardUpdateMonitorProvider = provider7;
        this.sysuiColorExtractorProvider = provider8;
        this.dockManagerProvider = provider9;
        this.blurUtilsProvider = provider10;
    }

    @Override // javax.inject.Provider
    public ScrimController get() {
        return provideInstance(this.lightBarControllerProvider, this.dozeParametersProvider, this.alarmManagerProvider, this.keyguardStateControllerProvider, this.delayedWakeLockBuilderProvider, this.handlerProvider, this.keyguardUpdateMonitorProvider, this.sysuiColorExtractorProvider, this.dockManagerProvider, this.blurUtilsProvider);
    }

    public static ScrimController provideInstance(Provider<LightBarController> provider, Provider<DozeParameters> provider2, Provider<AlarmManager> provider3, Provider<KeyguardStateController> provider4, Provider<DelayedWakeLock.Builder> provider5, Provider<Handler> provider6, Provider<KeyguardUpdateMonitor> provider7, Provider<SysuiColorExtractor> provider8, Provider<DockManager> provider9, Provider<BlurUtils> provider10) {
        return new ScrimController(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), provider9.get(), provider10.get());
    }

    public static ScrimController_Factory create(Provider<LightBarController> provider, Provider<DozeParameters> provider2, Provider<AlarmManager> provider3, Provider<KeyguardStateController> provider4, Provider<DelayedWakeLock.Builder> provider5, Provider<Handler> provider6, Provider<KeyguardUpdateMonitor> provider7, Provider<SysuiColorExtractor> provider8, Provider<DockManager> provider9, Provider<BlurUtils> provider10) {
        return new ScrimController_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10);
    }
}
