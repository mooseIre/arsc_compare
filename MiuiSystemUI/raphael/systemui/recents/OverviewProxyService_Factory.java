package com.android.systemui.recents;

import android.content.Context;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.model.SysUiState;
import com.android.systemui.pip.PipUI;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.NavigationBarController;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.android.systemui.statusbar.phone.NotificationShadeWindowController;
import com.android.systemui.statusbar.phone.StatusBar;
import dagger.Lazy;
import dagger.internal.Factory;
import java.util.Optional;
import javax.inject.Provider;

public final class OverviewProxyService_Factory implements Factory<OverviewProxyService> {
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<CommandQueue> commandQueueProvider;
    private final Provider<Context> contextProvider;
    private final Provider<Optional<Divider>> dividerOptionalProvider;
    private final Provider<NavigationBarController> navBarControllerProvider;
    private final Provider<NavigationModeController> navModeControllerProvider;
    private final Provider<PipUI> pipUIProvider;
    private final Provider<Optional<Lazy<StatusBar>>> statusBarOptionalLazyProvider;
    private final Provider<NotificationShadeWindowController> statusBarWinControllerProvider;
    private final Provider<SysUiState> sysUiStateProvider;

    public OverviewProxyService_Factory(Provider<Context> provider, Provider<CommandQueue> provider2, Provider<NavigationBarController> provider3, Provider<NavigationModeController> provider4, Provider<NotificationShadeWindowController> provider5, Provider<SysUiState> provider6, Provider<PipUI> provider7, Provider<Optional<Divider>> provider8, Provider<Optional<Lazy<StatusBar>>> provider9, Provider<BroadcastDispatcher> provider10) {
        this.contextProvider = provider;
        this.commandQueueProvider = provider2;
        this.navBarControllerProvider = provider3;
        this.navModeControllerProvider = provider4;
        this.statusBarWinControllerProvider = provider5;
        this.sysUiStateProvider = provider6;
        this.pipUIProvider = provider7;
        this.dividerOptionalProvider = provider8;
        this.statusBarOptionalLazyProvider = provider9;
        this.broadcastDispatcherProvider = provider10;
    }

    @Override // javax.inject.Provider
    public OverviewProxyService get() {
        return provideInstance(this.contextProvider, this.commandQueueProvider, this.navBarControllerProvider, this.navModeControllerProvider, this.statusBarWinControllerProvider, this.sysUiStateProvider, this.pipUIProvider, this.dividerOptionalProvider, this.statusBarOptionalLazyProvider, this.broadcastDispatcherProvider);
    }

    public static OverviewProxyService provideInstance(Provider<Context> provider, Provider<CommandQueue> provider2, Provider<NavigationBarController> provider3, Provider<NavigationModeController> provider4, Provider<NotificationShadeWindowController> provider5, Provider<SysUiState> provider6, Provider<PipUI> provider7, Provider<Optional<Divider>> provider8, Provider<Optional<Lazy<StatusBar>>> provider9, Provider<BroadcastDispatcher> provider10) {
        return new OverviewProxyService(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), provider9.get(), provider10.get());
    }

    public static OverviewProxyService_Factory create(Provider<Context> provider, Provider<CommandQueue> provider2, Provider<NavigationBarController> provider3, Provider<NavigationModeController> provider4, Provider<NotificationShadeWindowController> provider5, Provider<SysUiState> provider6, Provider<PipUI> provider7, Provider<Optional<Divider>> provider8, Provider<Optional<Lazy<StatusBar>>> provider9, Provider<BroadcastDispatcher> provider10) {
        return new OverviewProxyService_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10);
    }
}
