package com.android.systemui.pip.phone;

import android.content.Context;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.model.SysUiState;
import com.android.systemui.pip.PipBoundsHandler;
import com.android.systemui.pip.PipSnapAlgorithm;
import com.android.systemui.pip.PipTaskOrganizer;
import com.android.systemui.pip.PipUiEventLogger;
import com.android.systemui.util.DeviceConfigProxy;
import com.android.systemui.util.FloatingContentCoordinator;
import com.android.systemui.wm.DisplayController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class PipManager_Factory implements Factory<PipManager> {
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<Context> contextProvider;
    private final Provider<DeviceConfigProxy> deviceConfigProvider;
    private final Provider<DisplayController> displayControllerProvider;
    private final Provider<FloatingContentCoordinator> floatingContentCoordinatorProvider;
    private final Provider<PipBoundsHandler> pipBoundsHandlerProvider;
    private final Provider<PipSnapAlgorithm> pipSnapAlgorithmProvider;
    private final Provider<PipTaskOrganizer> pipTaskOrganizerProvider;
    private final Provider<PipUiEventLogger> pipUiEventLoggerProvider;
    private final Provider<SysUiState> sysUiStateProvider;

    public PipManager_Factory(Provider<Context> provider, Provider<BroadcastDispatcher> provider2, Provider<DisplayController> provider3, Provider<FloatingContentCoordinator> provider4, Provider<DeviceConfigProxy> provider5, Provider<PipBoundsHandler> provider6, Provider<PipSnapAlgorithm> provider7, Provider<PipTaskOrganizer> provider8, Provider<SysUiState> provider9, Provider<PipUiEventLogger> provider10) {
        this.contextProvider = provider;
        this.broadcastDispatcherProvider = provider2;
        this.displayControllerProvider = provider3;
        this.floatingContentCoordinatorProvider = provider4;
        this.deviceConfigProvider = provider5;
        this.pipBoundsHandlerProvider = provider6;
        this.pipSnapAlgorithmProvider = provider7;
        this.pipTaskOrganizerProvider = provider8;
        this.sysUiStateProvider = provider9;
        this.pipUiEventLoggerProvider = provider10;
    }

    @Override // javax.inject.Provider
    public PipManager get() {
        return provideInstance(this.contextProvider, this.broadcastDispatcherProvider, this.displayControllerProvider, this.floatingContentCoordinatorProvider, this.deviceConfigProvider, this.pipBoundsHandlerProvider, this.pipSnapAlgorithmProvider, this.pipTaskOrganizerProvider, this.sysUiStateProvider, this.pipUiEventLoggerProvider);
    }

    public static PipManager provideInstance(Provider<Context> provider, Provider<BroadcastDispatcher> provider2, Provider<DisplayController> provider3, Provider<FloatingContentCoordinator> provider4, Provider<DeviceConfigProxy> provider5, Provider<PipBoundsHandler> provider6, Provider<PipSnapAlgorithm> provider7, Provider<PipTaskOrganizer> provider8, Provider<SysUiState> provider9, Provider<PipUiEventLogger> provider10) {
        return new PipManager(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), provider9.get(), provider10.get());
    }

    public static PipManager_Factory create(Provider<Context> provider, Provider<BroadcastDispatcher> provider2, Provider<DisplayController> provider3, Provider<FloatingContentCoordinator> provider4, Provider<DeviceConfigProxy> provider5, Provider<PipBoundsHandler> provider6, Provider<PipSnapAlgorithm> provider7, Provider<PipTaskOrganizer> provider8, Provider<SysUiState> provider9, Provider<PipUiEventLogger> provider10) {
        return new PipManager_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10);
    }
}
