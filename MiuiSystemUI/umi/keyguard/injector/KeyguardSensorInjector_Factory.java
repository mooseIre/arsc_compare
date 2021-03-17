package com.android.keyguard.injector;

import android.content.Context;
import android.os.PowerManager;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class KeyguardSensorInjector_Factory implements Factory<KeyguardSensorInjector> {
    private final Provider<Context> mContextProvider;
    private final Provider<KeyguardUpdateMonitor> mKeyguardUpdateMonitorProvider;
    private final Provider<KeyguardViewMediator> mKeyguardViewMediatorProvider;
    private final Provider<PowerManager> mPowerManagerProvider;
    private final Provider<WakefulnessLifecycle> wakefulnessLifecycleProvider;

    public KeyguardSensorInjector_Factory(Provider<Context> provider, Provider<KeyguardViewMediator> provider2, Provider<PowerManager> provider3, Provider<KeyguardUpdateMonitor> provider4, Provider<WakefulnessLifecycle> provider5) {
        this.mContextProvider = provider;
        this.mKeyguardViewMediatorProvider = provider2;
        this.mPowerManagerProvider = provider3;
        this.mKeyguardUpdateMonitorProvider = provider4;
        this.wakefulnessLifecycleProvider = provider5;
    }

    @Override // javax.inject.Provider
    public KeyguardSensorInjector get() {
        return provideInstance(this.mContextProvider, this.mKeyguardViewMediatorProvider, this.mPowerManagerProvider, this.mKeyguardUpdateMonitorProvider, this.wakefulnessLifecycleProvider);
    }

    public static KeyguardSensorInjector provideInstance(Provider<Context> provider, Provider<KeyguardViewMediator> provider2, Provider<PowerManager> provider3, Provider<KeyguardUpdateMonitor> provider4, Provider<WakefulnessLifecycle> provider5) {
        return new KeyguardSensorInjector(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get());
    }

    public static KeyguardSensorInjector_Factory create(Provider<Context> provider, Provider<KeyguardViewMediator> provider2, Provider<PowerManager> provider3, Provider<KeyguardUpdateMonitor> provider4, Provider<WakefulnessLifecycle> provider5) {
        return new KeyguardSensorInjector_Factory(provider, provider2, provider3, provider4, provider5);
    }
}
