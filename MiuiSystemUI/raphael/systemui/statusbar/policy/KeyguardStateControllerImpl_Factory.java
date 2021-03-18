package com.android.systemui.statusbar.policy;

import android.content.Context;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class KeyguardStateControllerImpl_Factory implements Factory<KeyguardStateControllerImpl> {
    private final Provider<Context> contextProvider;
    private final Provider<KeyguardUpdateMonitor> keyguardUpdateMonitorProvider;
    private final Provider<LockPatternUtils> lockPatternUtilsProvider;

    public KeyguardStateControllerImpl_Factory(Provider<Context> provider, Provider<KeyguardUpdateMonitor> provider2, Provider<LockPatternUtils> provider3) {
        this.contextProvider = provider;
        this.keyguardUpdateMonitorProvider = provider2;
        this.lockPatternUtilsProvider = provider3;
    }

    @Override // javax.inject.Provider
    public KeyguardStateControllerImpl get() {
        return provideInstance(this.contextProvider, this.keyguardUpdateMonitorProvider, this.lockPatternUtilsProvider);
    }

    public static KeyguardStateControllerImpl provideInstance(Provider<Context> provider, Provider<KeyguardUpdateMonitor> provider2, Provider<LockPatternUtils> provider3) {
        return new KeyguardStateControllerImpl(provider.get(), provider2.get(), provider3.get());
    }

    public static KeyguardStateControllerImpl_Factory create(Provider<Context> provider, Provider<KeyguardUpdateMonitor> provider2, Provider<LockPatternUtils> provider3) {
        return new KeyguardStateControllerImpl_Factory(provider, provider2, provider3);
    }
}
