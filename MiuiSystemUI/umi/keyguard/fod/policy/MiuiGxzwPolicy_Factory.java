package com.android.keyguard.fod.policy;

import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.phone.StatusBar;
import com.miui.systemui.statusbar.PanelExpansionObserver;
import dagger.internal.DoubleCheck;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiGxzwPolicy_Factory implements Factory<MiuiGxzwPolicy> {
    private final Provider<PanelExpansionObserver> panelExpansionObserverProvider;
    private final Provider<StatusBar> statusBarLazyProvider;
    private final Provider<StatusBarStateController> statusBarStateControllerProvider;

    public MiuiGxzwPolicy_Factory(Provider<PanelExpansionObserver> provider, Provider<StatusBarStateController> provider2, Provider<StatusBar> provider3) {
        this.panelExpansionObserverProvider = provider;
        this.statusBarStateControllerProvider = provider2;
        this.statusBarLazyProvider = provider3;
    }

    @Override // javax.inject.Provider
    public MiuiGxzwPolicy get() {
        return provideInstance(this.panelExpansionObserverProvider, this.statusBarStateControllerProvider, this.statusBarLazyProvider);
    }

    public static MiuiGxzwPolicy provideInstance(Provider<PanelExpansionObserver> provider, Provider<StatusBarStateController> provider2, Provider<StatusBar> provider3) {
        return new MiuiGxzwPolicy(provider.get(), provider2.get(), DoubleCheck.lazy(provider3));
    }

    public static MiuiGxzwPolicy_Factory create(Provider<PanelExpansionObserver> provider, Provider<StatusBarStateController> provider2, Provider<StatusBar> provider3) {
        return new MiuiGxzwPolicy_Factory(provider, provider2, provider3);
    }
}
