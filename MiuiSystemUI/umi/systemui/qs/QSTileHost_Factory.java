package com.android.systemui.qs;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.android.internal.logging.UiEventLogger;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.controlcenter.phone.ControlPanelController;
import com.android.systemui.controlcenter.qs.MiuiQSTileHostInjector;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.plugins.qs.QSFactory;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.qs.logging.QSLogger;
import com.android.systemui.shared.plugins.PluginManager;
import com.android.systemui.statusbar.phone.AutoTileManager;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.tuner.TunerService;
import dagger.internal.Factory;
import java.util.Optional;
import java.util.concurrent.Executor;
import javax.inject.Provider;

public final class QSTileHost_Factory implements Factory<QSTileHost> {
    private final Provider<AutoTileManager> autoTilesProvider;
    private final Provider<Executor> bgExecutorProvider;
    private final Provider<Looper> bgLooperProvider;
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<Context> contextProvider;
    private final Provider<ControlPanelController> controlPanelControllerProvider;
    private final Provider<QSFactory> defaultFactoryProvider;
    private final Provider<DumpManager> dumpManagerProvider;
    private final Provider<MiuiQSTileHostInjector> hostInjectorProvider;
    private final Provider<StatusBarIconController> iconControllerProvider;
    private final Provider<Handler> mainHandlerProvider;
    private final Provider<PluginManager> pluginManagerProvider;
    private final Provider<QSLogger> qsLoggerProvider;
    private final Provider<Optional<StatusBar>> statusBarOptionalProvider;
    private final Provider<StatusBarStateController> statusBarStateControllerProvider;
    private final Provider<TunerService> tunerServiceProvider;
    private final Provider<UiEventLogger> uiEventLoggerProvider;

    public QSTileHost_Factory(Provider<Context> provider, Provider<StatusBarIconController> provider2, Provider<QSFactory> provider3, Provider<Handler> provider4, Provider<Looper> provider5, Provider<Executor> provider6, Provider<PluginManager> provider7, Provider<TunerService> provider8, Provider<AutoTileManager> provider9, Provider<DumpManager> provider10, Provider<BroadcastDispatcher> provider11, Provider<Optional<StatusBar>> provider12, Provider<QSLogger> provider13, Provider<UiEventLogger> provider14, Provider<StatusBarStateController> provider15, Provider<MiuiQSTileHostInjector> provider16, Provider<ControlPanelController> provider17) {
        this.contextProvider = provider;
        this.iconControllerProvider = provider2;
        this.defaultFactoryProvider = provider3;
        this.mainHandlerProvider = provider4;
        this.bgLooperProvider = provider5;
        this.bgExecutorProvider = provider6;
        this.pluginManagerProvider = provider7;
        this.tunerServiceProvider = provider8;
        this.autoTilesProvider = provider9;
        this.dumpManagerProvider = provider10;
        this.broadcastDispatcherProvider = provider11;
        this.statusBarOptionalProvider = provider12;
        this.qsLoggerProvider = provider13;
        this.uiEventLoggerProvider = provider14;
        this.statusBarStateControllerProvider = provider15;
        this.hostInjectorProvider = provider16;
        this.controlPanelControllerProvider = provider17;
    }

    @Override // javax.inject.Provider
    public QSTileHost get() {
        return provideInstance(this.contextProvider, this.iconControllerProvider, this.defaultFactoryProvider, this.mainHandlerProvider, this.bgLooperProvider, this.bgExecutorProvider, this.pluginManagerProvider, this.tunerServiceProvider, this.autoTilesProvider, this.dumpManagerProvider, this.broadcastDispatcherProvider, this.statusBarOptionalProvider, this.qsLoggerProvider, this.uiEventLoggerProvider, this.statusBarStateControllerProvider, this.hostInjectorProvider, this.controlPanelControllerProvider);
    }

    public static QSTileHost provideInstance(Provider<Context> provider, Provider<StatusBarIconController> provider2, Provider<QSFactory> provider3, Provider<Handler> provider4, Provider<Looper> provider5, Provider<Executor> provider6, Provider<PluginManager> provider7, Provider<TunerService> provider8, Provider<AutoTileManager> provider9, Provider<DumpManager> provider10, Provider<BroadcastDispatcher> provider11, Provider<Optional<StatusBar>> provider12, Provider<QSLogger> provider13, Provider<UiEventLogger> provider14, Provider<StatusBarStateController> provider15, Provider<MiuiQSTileHostInjector> provider16, Provider<ControlPanelController> provider17) {
        return new QSTileHost(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), provider9, provider10.get(), provider11.get(), provider12.get(), provider13.get(), provider14.get(), provider15.get(), provider16.get(), provider17.get());
    }

    public static QSTileHost_Factory create(Provider<Context> provider, Provider<StatusBarIconController> provider2, Provider<QSFactory> provider3, Provider<Handler> provider4, Provider<Looper> provider5, Provider<Executor> provider6, Provider<PluginManager> provider7, Provider<TunerService> provider8, Provider<AutoTileManager> provider9, Provider<DumpManager> provider10, Provider<BroadcastDispatcher> provider11, Provider<Optional<StatusBar>> provider12, Provider<QSLogger> provider13, Provider<UiEventLogger> provider14, Provider<StatusBarStateController> provider15, Provider<MiuiQSTileHostInjector> provider16, Provider<ControlPanelController> provider17) {
        return new QSTileHost_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10, provider11, provider12, provider13, provider14, provider15, provider16, provider17);
    }
}
