package com.android.keyguard.fod;

import android.content.Context;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiGxzwManager_Factory implements Factory<MiuiGxzwManager> {
    private final Provider<Context> contextProvider;
    private final Provider<WakefulnessLifecycle> wakefulnessObserverProvider;

    public MiuiGxzwManager_Factory(Provider<Context> provider, Provider<WakefulnessLifecycle> provider2) {
        this.contextProvider = provider;
        this.wakefulnessObserverProvider = provider2;
    }

    @Override // javax.inject.Provider
    public MiuiGxzwManager get() {
        return provideInstance(this.contextProvider, this.wakefulnessObserverProvider);
    }

    public static MiuiGxzwManager provideInstance(Provider<Context> provider, Provider<WakefulnessLifecycle> provider2) {
        return new MiuiGxzwManager(provider.get(), provider2.get());
    }

    public static MiuiGxzwManager_Factory create(Provider<Context> provider, Provider<WakefulnessLifecycle> provider2) {
        return new MiuiGxzwManager_Factory(provider, provider2);
    }
}
