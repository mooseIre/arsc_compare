package com.android.keyguard.charge;

import android.content.Context;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiChargeController_Factory implements Factory<MiuiChargeController> {
    private final Provider<Context> contextProvider;
    private final Provider<WakefulnessLifecycle> wakefulnessLifecycleProvider;

    public MiuiChargeController_Factory(Provider<Context> provider, Provider<WakefulnessLifecycle> provider2) {
        this.contextProvider = provider;
        this.wakefulnessLifecycleProvider = provider2;
    }

    @Override // javax.inject.Provider
    public MiuiChargeController get() {
        return provideInstance(this.contextProvider, this.wakefulnessLifecycleProvider);
    }

    public static MiuiChargeController provideInstance(Provider<Context> provider, Provider<WakefulnessLifecycle> provider2) {
        return new MiuiChargeController(provider.get(), provider2.get());
    }

    public static MiuiChargeController_Factory create(Provider<Context> provider, Provider<WakefulnessLifecycle> provider2) {
        return new MiuiChargeController_Factory(provider, provider2);
    }
}
