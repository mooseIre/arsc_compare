package com.android.systemui.controlcenter.qs;

import android.content.Context;
import com.android.systemui.controlcenter.phone.ControlPanelController;
import com.android.systemui.controlcenter.policy.OldModeController;
import com.android.systemui.controlcenter.policy.SuperSaveModeController;
import com.android.systemui.shared.plugins.PluginManager;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.tuner.TunerService;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiQSTileHostInjector_Factory implements Factory<MiuiQSTileHostInjector> {
    private final Provider<Context> contextProvider;
    private final Provider<ControlPanelController> controllerProvider;
    private final Provider<DeviceProvisionedController> deviceProvisionedControllerProvider;
    private final Provider<OldModeController> oldModeControllerProvider;
    private final Provider<PluginManager> pluginManagerProvider;
    private final Provider<SuperSaveModeController> superSaveModeControllerProvider;
    private final Provider<TunerService> tunerServiceProvider;

    public MiuiQSTileHostInjector_Factory(Provider<Context> provider, Provider<PluginManager> provider2, Provider<TunerService> provider3, Provider<ControlPanelController> provider4, Provider<SuperSaveModeController> provider5, Provider<OldModeController> provider6, Provider<DeviceProvisionedController> provider7) {
        this.contextProvider = provider;
        this.pluginManagerProvider = provider2;
        this.tunerServiceProvider = provider3;
        this.controllerProvider = provider4;
        this.superSaveModeControllerProvider = provider5;
        this.oldModeControllerProvider = provider6;
        this.deviceProvisionedControllerProvider = provider7;
    }

    @Override // javax.inject.Provider
    public MiuiQSTileHostInjector get() {
        return provideInstance(this.contextProvider, this.pluginManagerProvider, this.tunerServiceProvider, this.controllerProvider, this.superSaveModeControllerProvider, this.oldModeControllerProvider, this.deviceProvisionedControllerProvider);
    }

    public static MiuiQSTileHostInjector provideInstance(Provider<Context> provider, Provider<PluginManager> provider2, Provider<TunerService> provider3, Provider<ControlPanelController> provider4, Provider<SuperSaveModeController> provider5, Provider<OldModeController> provider6, Provider<DeviceProvisionedController> provider7) {
        return new MiuiQSTileHostInjector(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get());
    }

    public static MiuiQSTileHostInjector_Factory create(Provider<Context> provider, Provider<PluginManager> provider2, Provider<TunerService> provider3, Provider<ControlPanelController> provider4, Provider<SuperSaveModeController> provider5, Provider<OldModeController> provider6, Provider<DeviceProvisionedController> provider7) {
        return new MiuiQSTileHostInjector_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7);
    }
}
