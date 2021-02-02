package com.android.systemui.controlcenter.policy;

import android.content.Context;
import com.android.systemui.controlcenter.phone.ControlPanelController;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.phone.ShadeController;
import com.miui.systemui.analytics.SystemUIStat;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NCSwitchController_Factory implements Factory<NCSwitchController> {
    private final Provider<Context> mContextProvider;
    private final Provider<ControlPanelController> mControlPanelControllerProvider;
    private final Provider<SysuiStatusBarStateController> mStatusBarStateControllerProvider;
    private final Provider<ShadeController> shadeCollerProvider;
    private final Provider<SystemUIStat> systemUIStatProvider;

    public NCSwitchController_Factory(Provider<Context> provider, Provider<SysuiStatusBarStateController> provider2, Provider<ControlPanelController> provider3, Provider<ShadeController> provider4, Provider<SystemUIStat> provider5) {
        this.mContextProvider = provider;
        this.mStatusBarStateControllerProvider = provider2;
        this.mControlPanelControllerProvider = provider3;
        this.shadeCollerProvider = provider4;
        this.systemUIStatProvider = provider5;
    }

    public NCSwitchController get() {
        return provideInstance(this.mContextProvider, this.mStatusBarStateControllerProvider, this.mControlPanelControllerProvider, this.shadeCollerProvider, this.systemUIStatProvider);
    }

    public static NCSwitchController provideInstance(Provider<Context> provider, Provider<SysuiStatusBarStateController> provider2, Provider<ControlPanelController> provider3, Provider<ShadeController> provider4, Provider<SystemUIStat> provider5) {
        return new NCSwitchController(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get());
    }

    public static NCSwitchController_Factory create(Provider<Context> provider, Provider<SysuiStatusBarStateController> provider2, Provider<ControlPanelController> provider3, Provider<ShadeController> provider4, Provider<SystemUIStat> provider5) {
        return new NCSwitchController_Factory(provider, provider2, provider3, provider4, provider5);
    }
}
