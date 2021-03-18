package com.android.systemui.controlcenter.policy;

import android.content.Context;
import com.android.systemui.controlcenter.phone.ControlPanelController;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.phone.HeadsUpManagerPhone;
import com.android.systemui.statusbar.phone.ShadeController;
import com.miui.systemui.analytics.SystemUIStat;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NCSwitchController_Factory implements Factory<NCSwitchController> {
    private final Provider<Context> mContextProvider;
    private final Provider<ControlPanelController> mControlPanelControllerProvider;
    private final Provider<HeadsUpManagerPhone> mHeadsUpManagerProvider;
    private final Provider<SysuiStatusBarStateController> mStatusBarStateControllerProvider;
    private final Provider<ShadeController> shadeCollerProvider;
    private final Provider<SystemUIStat> systemUIStatProvider;

    public NCSwitchController_Factory(Provider<Context> provider, Provider<SysuiStatusBarStateController> provider2, Provider<ControlPanelController> provider3, Provider<ShadeController> provider4, Provider<HeadsUpManagerPhone> provider5, Provider<SystemUIStat> provider6) {
        this.mContextProvider = provider;
        this.mStatusBarStateControllerProvider = provider2;
        this.mControlPanelControllerProvider = provider3;
        this.shadeCollerProvider = provider4;
        this.mHeadsUpManagerProvider = provider5;
        this.systemUIStatProvider = provider6;
    }

    @Override // javax.inject.Provider
    public NCSwitchController get() {
        return provideInstance(this.mContextProvider, this.mStatusBarStateControllerProvider, this.mControlPanelControllerProvider, this.shadeCollerProvider, this.mHeadsUpManagerProvider, this.systemUIStatProvider);
    }

    public static NCSwitchController provideInstance(Provider<Context> provider, Provider<SysuiStatusBarStateController> provider2, Provider<ControlPanelController> provider3, Provider<ShadeController> provider4, Provider<HeadsUpManagerPhone> provider5, Provider<SystemUIStat> provider6) {
        return new NCSwitchController(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get());
    }

    public static NCSwitchController_Factory create(Provider<Context> provider, Provider<SysuiStatusBarStateController> provider2, Provider<ControlPanelController> provider3, Provider<ShadeController> provider4, Provider<HeadsUpManagerPhone> provider5, Provider<SystemUIStat> provider6) {
        return new NCSwitchController_Factory(provider, provider2, provider3, provider4, provider5, provider6);
    }
}
