package com.android.systemui.controlcenter.phone;

import android.content.Context;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.phone.HeadsUpManagerPhone;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.MiuiNotificationShadePolicy;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ControlPanelWindowManager_Factory implements Factory<ControlPanelWindowManager> {
    private final Provider<Context> contextProvider;
    private final Provider<ControlPanelController> controlPanelControllerProvider;
    private final Provider<HeadsUpManagerPhone> headsUpManagerPhoneProvider;
    private final Provider<MiuiNotificationShadePolicy> miuiNotificationShadePolicyProvider;
    private final Provider<StatusBar> statusBarProvider;
    private final Provider<StatusBarStateController> statusBarStateControllerProvider;

    public ControlPanelWindowManager_Factory(Provider<Context> provider, Provider<StatusBar> provider2, Provider<ControlPanelController> provider3, Provider<HeadsUpManagerPhone> provider4, Provider<StatusBarStateController> provider5, Provider<MiuiNotificationShadePolicy> provider6) {
        this.contextProvider = provider;
        this.statusBarProvider = provider2;
        this.controlPanelControllerProvider = provider3;
        this.headsUpManagerPhoneProvider = provider4;
        this.statusBarStateControllerProvider = provider5;
        this.miuiNotificationShadePolicyProvider = provider6;
    }

    @Override // javax.inject.Provider
    public ControlPanelWindowManager get() {
        return provideInstance(this.contextProvider, this.statusBarProvider, this.controlPanelControllerProvider, this.headsUpManagerPhoneProvider, this.statusBarStateControllerProvider, this.miuiNotificationShadePolicyProvider);
    }

    public static ControlPanelWindowManager provideInstance(Provider<Context> provider, Provider<StatusBar> provider2, Provider<ControlPanelController> provider3, Provider<HeadsUpManagerPhone> provider4, Provider<StatusBarStateController> provider5, Provider<MiuiNotificationShadePolicy> provider6) {
        return new ControlPanelWindowManager(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get());
    }

    public static ControlPanelWindowManager_Factory create(Provider<Context> provider, Provider<StatusBar> provider2, Provider<ControlPanelController> provider3, Provider<HeadsUpManagerPhone> provider4, Provider<StatusBarStateController> provider5, Provider<MiuiNotificationShadePolicy> provider6) {
        return new ControlPanelWindowManager_Factory(provider, provider2, provider3, provider4, provider5, provider6);
    }
}
