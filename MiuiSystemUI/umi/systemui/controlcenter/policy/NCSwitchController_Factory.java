package com.android.systemui.controlcenter.policy;

import android.content.Context;
import android.os.Handler;
import com.android.systemui.controlcenter.phone.ControlPanelController;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.phone.HeadsUpManagerPhone;
import com.android.systemui.statusbar.phone.PanelViewLogger;
import com.android.systemui.statusbar.phone.ShadeController;
import com.miui.systemui.analytics.SystemUIStat;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NCSwitchController_Factory implements Factory<NCSwitchController> {
    private final Provider<Context> mContextProvider;
    private final Provider<ControlPanelController> mControlPanelControllerProvider;
    private final Provider<Handler> mHandlerProvider;
    private final Provider<HeadsUpManagerPhone> mHeadsUpManagerProvider;
    private final Provider<SysuiStatusBarStateController> mStatusBarStateControllerProvider;
    private final Provider<PanelViewLogger> panelViewLoggerProvider;
    private final Provider<ShadeController> shadeCollerProvider;
    private final Provider<SystemUIStat> systemUIStatProvider;

    public NCSwitchController_Factory(Provider<Context> provider, Provider<SysuiStatusBarStateController> provider2, Provider<ControlPanelController> provider3, Provider<ShadeController> provider4, Provider<HeadsUpManagerPhone> provider5, Provider<Handler> provider6, Provider<PanelViewLogger> provider7, Provider<SystemUIStat> provider8) {
        this.mContextProvider = provider;
        this.mStatusBarStateControllerProvider = provider2;
        this.mControlPanelControllerProvider = provider3;
        this.shadeCollerProvider = provider4;
        this.mHeadsUpManagerProvider = provider5;
        this.mHandlerProvider = provider6;
        this.panelViewLoggerProvider = provider7;
        this.systemUIStatProvider = provider8;
    }

    @Override // javax.inject.Provider
    public NCSwitchController get() {
        return provideInstance(this.mContextProvider, this.mStatusBarStateControllerProvider, this.mControlPanelControllerProvider, this.shadeCollerProvider, this.mHeadsUpManagerProvider, this.mHandlerProvider, this.panelViewLoggerProvider, this.systemUIStatProvider);
    }

    public static NCSwitchController provideInstance(Provider<Context> provider, Provider<SysuiStatusBarStateController> provider2, Provider<ControlPanelController> provider3, Provider<ShadeController> provider4, Provider<HeadsUpManagerPhone> provider5, Provider<Handler> provider6, Provider<PanelViewLogger> provider7, Provider<SystemUIStat> provider8) {
        return new NCSwitchController(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get());
    }

    public static NCSwitchController_Factory create(Provider<Context> provider, Provider<SysuiStatusBarStateController> provider2, Provider<ControlPanelController> provider3, Provider<ShadeController> provider4, Provider<HeadsUpManagerPhone> provider5, Provider<Handler> provider6, Provider<PanelViewLogger> provider7, Provider<SystemUIStat> provider8) {
        return new NCSwitchController_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8);
    }
}
