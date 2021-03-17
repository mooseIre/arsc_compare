package com.android.keyguard.injector;

import android.content.Context;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.statusbar.phone.StatusBar;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class KeyguardPanelViewInjector_Factory implements Factory<KeyguardPanelViewInjector> {
    private final Provider<Context> mContextProvider;
    private final Provider<StatusBar> mStatusBarProvider;
    private final Provider<WakefulnessLifecycle> wakefulnessLifecycleProvider;

    public KeyguardPanelViewInjector_Factory(Provider<Context> provider, Provider<StatusBar> provider2, Provider<WakefulnessLifecycle> provider3) {
        this.mContextProvider = provider;
        this.mStatusBarProvider = provider2;
        this.wakefulnessLifecycleProvider = provider3;
    }

    @Override // javax.inject.Provider
    public KeyguardPanelViewInjector get() {
        return provideInstance(this.mContextProvider, this.mStatusBarProvider, this.wakefulnessLifecycleProvider);
    }

    public static KeyguardPanelViewInjector provideInstance(Provider<Context> provider, Provider<StatusBar> provider2, Provider<WakefulnessLifecycle> provider3) {
        return new KeyguardPanelViewInjector(provider.get(), provider2.get(), provider3.get());
    }

    public static KeyguardPanelViewInjector_Factory create(Provider<Context> provider, Provider<StatusBar> provider2, Provider<WakefulnessLifecycle> provider3) {
        return new KeyguardPanelViewInjector_Factory(provider, provider2, provider3);
    }
}
