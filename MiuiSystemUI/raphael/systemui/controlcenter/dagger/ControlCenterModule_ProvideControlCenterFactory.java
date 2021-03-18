package com.android.systemui.controlcenter.dagger;

import android.content.Context;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.controlcenter.ControlCenter;
import com.android.systemui.controlcenter.phone.ControlPanelController;
import com.android.systemui.controlcenter.phone.ControlPanelWindowManager;
import com.android.systemui.controlcenter.phone.ExpandInfoController;
import com.android.systemui.controlcenter.phone.controls.ControlsPluginManager;
import com.android.systemui.controlcenter.policy.ControlCenterActivityStarter;
import com.android.systemui.controlcenter.policy.SuperSaveModeController;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.util.InjectionInflationController;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class ControlCenterModule_ProvideControlCenterFactory implements Factory<ControlCenter> {
    private final Provider<ActivityStarter> activityStarterProvider;
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<CommandQueue> commandQueueProvider;
    private final Provider<ConfigurationController> configurationControllerProvider;
    private final Provider<Context> contextProvider;
    private final Provider<ControlCenterActivityStarter> controlCenterActivityStarterProvider;
    private final Provider<ControlPanelController> controlPanelControllerProvider;
    private final Provider<ControlPanelWindowManager> controlPanelWindowManagerProvider;
    private final Provider<ControlsPluginManager> controlsPluginManagerProvider;
    private final Provider<ExpandInfoController> expandInfoControllerProvider;
    private final Provider<StatusBarIconController> iconControllerProvider;
    private final Provider<InjectionInflationController> injectionInflaterProvider;
    private final Provider<QSTileHost> qsControlTileHostProvider;
    private final Provider<StatusBar> statusBarProvider;
    private final Provider<SuperSaveModeController> superSaveModeControllerProvider;

    public ControlCenterModule_ProvideControlCenterFactory(Provider<Context> provider, Provider<ControlPanelController> provider2, Provider<StatusBarIconController> provider3, Provider<ExpandInfoController> provider4, Provider<ActivityStarter> provider5, Provider<CommandQueue> provider6, Provider<InjectionInflationController> provider7, Provider<SuperSaveModeController> provider8, Provider<ControlCenterActivityStarter> provider9, Provider<QSTileHost> provider10, Provider<ControlPanelWindowManager> provider11, Provider<StatusBar> provider12, Provider<ControlsPluginManager> provider13, Provider<BroadcastDispatcher> provider14, Provider<ConfigurationController> provider15) {
        this.contextProvider = provider;
        this.controlPanelControllerProvider = provider2;
        this.iconControllerProvider = provider3;
        this.expandInfoControllerProvider = provider4;
        this.activityStarterProvider = provider5;
        this.commandQueueProvider = provider6;
        this.injectionInflaterProvider = provider7;
        this.superSaveModeControllerProvider = provider8;
        this.controlCenterActivityStarterProvider = provider9;
        this.qsControlTileHostProvider = provider10;
        this.controlPanelWindowManagerProvider = provider11;
        this.statusBarProvider = provider12;
        this.controlsPluginManagerProvider = provider13;
        this.broadcastDispatcherProvider = provider14;
        this.configurationControllerProvider = provider15;
    }

    @Override // javax.inject.Provider
    public ControlCenter get() {
        return provideInstance(this.contextProvider, this.controlPanelControllerProvider, this.iconControllerProvider, this.expandInfoControllerProvider, this.activityStarterProvider, this.commandQueueProvider, this.injectionInflaterProvider, this.superSaveModeControllerProvider, this.controlCenterActivityStarterProvider, this.qsControlTileHostProvider, this.controlPanelWindowManagerProvider, this.statusBarProvider, this.controlsPluginManagerProvider, this.broadcastDispatcherProvider, this.configurationControllerProvider);
    }

    public static ControlCenter provideInstance(Provider<Context> provider, Provider<ControlPanelController> provider2, Provider<StatusBarIconController> provider3, Provider<ExpandInfoController> provider4, Provider<ActivityStarter> provider5, Provider<CommandQueue> provider6, Provider<InjectionInflationController> provider7, Provider<SuperSaveModeController> provider8, Provider<ControlCenterActivityStarter> provider9, Provider<QSTileHost> provider10, Provider<ControlPanelWindowManager> provider11, Provider<StatusBar> provider12, Provider<ControlsPluginManager> provider13, Provider<BroadcastDispatcher> provider14, Provider<ConfigurationController> provider15) {
        return proxyProvideControlCenter(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), provider9.get(), provider10.get(), provider11.get(), provider12.get(), provider13.get(), provider14.get(), provider15.get());
    }

    public static ControlCenterModule_ProvideControlCenterFactory create(Provider<Context> provider, Provider<ControlPanelController> provider2, Provider<StatusBarIconController> provider3, Provider<ExpandInfoController> provider4, Provider<ActivityStarter> provider5, Provider<CommandQueue> provider6, Provider<InjectionInflationController> provider7, Provider<SuperSaveModeController> provider8, Provider<ControlCenterActivityStarter> provider9, Provider<QSTileHost> provider10, Provider<ControlPanelWindowManager> provider11, Provider<StatusBar> provider12, Provider<ControlsPluginManager> provider13, Provider<BroadcastDispatcher> provider14, Provider<ConfigurationController> provider15) {
        return new ControlCenterModule_ProvideControlCenterFactory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10, provider11, provider12, provider13, provider14, provider15);
    }

    public static ControlCenter proxyProvideControlCenter(Context context, ControlPanelController controlPanelController, StatusBarIconController statusBarIconController, ExpandInfoController expandInfoController, ActivityStarter activityStarter, CommandQueue commandQueue, InjectionInflationController injectionInflationController, SuperSaveModeController superSaveModeController, ControlCenterActivityStarter controlCenterActivityStarter, QSTileHost qSTileHost, ControlPanelWindowManager controlPanelWindowManager, StatusBar statusBar, ControlsPluginManager controlsPluginManager, BroadcastDispatcher broadcastDispatcher, ConfigurationController configurationController) {
        ControlCenter provideControlCenter = ControlCenterModule.provideControlCenter(context, controlPanelController, statusBarIconController, expandInfoController, activityStarter, commandQueue, injectionInflationController, superSaveModeController, controlCenterActivityStarter, qSTileHost, controlPanelWindowManager, statusBar, controlsPluginManager, broadcastDispatcher, configurationController);
        Preconditions.checkNotNull(provideControlCenter, "Cannot return null from a non-@Nullable @Provides method");
        return provideControlCenter;
    }
}
