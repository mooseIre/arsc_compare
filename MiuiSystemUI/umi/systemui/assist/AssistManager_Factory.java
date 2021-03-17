package com.android.systemui.assist;

import android.content.Context;
import com.android.internal.app.AssistUtils;
import com.android.systemui.assist.ui.DefaultUiController;
import com.android.systemui.model.SysUiState;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import dagger.internal.DoubleCheck;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class AssistManager_Factory implements Factory<AssistManager> {
    private final Provider<AssistLogger> assistLoggerProvider;
    private final Provider<AssistUtils> assistUtilsProvider;
    private final Provider<CommandQueue> commandQueueProvider;
    private final Provider<ConfigurationController> configurationControllerProvider;
    private final Provider<Context> contextProvider;
    private final Provider<DeviceProvisionedController> controllerProvider;
    private final Provider<DefaultUiController> defaultUiControllerProvider;
    private final Provider<AssistHandleBehaviorController> handleControllerProvider;
    private final Provider<OverviewProxyService> overviewProxyServiceProvider;
    private final Provider<PhoneStateMonitor> phoneStateMonitorProvider;
    private final Provider<SysUiState> sysUiStateProvider;

    public AssistManager_Factory(Provider<DeviceProvisionedController> provider, Provider<Context> provider2, Provider<AssistUtils> provider3, Provider<AssistHandleBehaviorController> provider4, Provider<CommandQueue> provider5, Provider<PhoneStateMonitor> provider6, Provider<OverviewProxyService> provider7, Provider<ConfigurationController> provider8, Provider<SysUiState> provider9, Provider<DefaultUiController> provider10, Provider<AssistLogger> provider11) {
        this.controllerProvider = provider;
        this.contextProvider = provider2;
        this.assistUtilsProvider = provider3;
        this.handleControllerProvider = provider4;
        this.commandQueueProvider = provider5;
        this.phoneStateMonitorProvider = provider6;
        this.overviewProxyServiceProvider = provider7;
        this.configurationControllerProvider = provider8;
        this.sysUiStateProvider = provider9;
        this.defaultUiControllerProvider = provider10;
        this.assistLoggerProvider = provider11;
    }

    @Override // javax.inject.Provider
    public AssistManager get() {
        return provideInstance(this.controllerProvider, this.contextProvider, this.assistUtilsProvider, this.handleControllerProvider, this.commandQueueProvider, this.phoneStateMonitorProvider, this.overviewProxyServiceProvider, this.configurationControllerProvider, this.sysUiStateProvider, this.defaultUiControllerProvider, this.assistLoggerProvider);
    }

    public static AssistManager provideInstance(Provider<DeviceProvisionedController> provider, Provider<Context> provider2, Provider<AssistUtils> provider3, Provider<AssistHandleBehaviorController> provider4, Provider<CommandQueue> provider5, Provider<PhoneStateMonitor> provider6, Provider<OverviewProxyService> provider7, Provider<ConfigurationController> provider8, Provider<SysUiState> provider9, Provider<DefaultUiController> provider10, Provider<AssistLogger> provider11) {
        return new AssistManager(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), DoubleCheck.lazy(provider9), provider10.get(), provider11.get());
    }

    public static AssistManager_Factory create(Provider<DeviceProvisionedController> provider, Provider<Context> provider2, Provider<AssistUtils> provider3, Provider<AssistHandleBehaviorController> provider4, Provider<CommandQueue> provider5, Provider<PhoneStateMonitor> provider6, Provider<OverviewProxyService> provider7, Provider<ConfigurationController> provider8, Provider<SysUiState> provider9, Provider<DefaultUiController> provider10, Provider<AssistLogger> provider11) {
        return new AssistManager_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10, provider11);
    }
}
