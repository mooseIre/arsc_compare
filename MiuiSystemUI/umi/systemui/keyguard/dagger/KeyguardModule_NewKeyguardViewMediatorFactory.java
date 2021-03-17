package com.android.systemui.keyguard.dagger;

import android.app.trust.TrustManager;
import android.content.Context;
import android.os.PowerManager;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardViewController;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.keyguard.DismissCallbackRegistry;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.android.systemui.util.DeviceConfigProxy;
import dagger.Lazy;
import dagger.internal.DoubleCheck;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import java.util.concurrent.Executor;
import javax.inject.Provider;

public final class KeyguardModule_NewKeyguardViewMediatorFactory implements Factory<KeyguardViewMediator> {
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<Context> contextProvider;
    private final Provider<DeviceConfigProxy> deviceConfigProvider;
    private final Provider<DismissCallbackRegistry> dismissCallbackRegistryProvider;
    private final Provider<DumpManager> dumpManagerProvider;
    private final Provider<FalsingManager> falsingManagerProvider;
    private final Provider<LockPatternUtils> lockPatternUtilsProvider;
    private final Provider<NavigationModeController> navigationModeControllerProvider;
    private final Provider<PowerManager> powerManagerProvider;
    private final Provider<KeyguardViewController> statusBarKeyguardViewManagerLazyProvider;
    private final Provider<TrustManager> trustManagerProvider;
    private final Provider<Executor> uiBgExecutorProvider;
    private final Provider<KeyguardUpdateMonitor> updateMonitorProvider;

    public KeyguardModule_NewKeyguardViewMediatorFactory(Provider<Context> provider, Provider<FalsingManager> provider2, Provider<LockPatternUtils> provider3, Provider<BroadcastDispatcher> provider4, Provider<KeyguardViewController> provider5, Provider<DismissCallbackRegistry> provider6, Provider<KeyguardUpdateMonitor> provider7, Provider<DumpManager> provider8, Provider<PowerManager> provider9, Provider<TrustManager> provider10, Provider<Executor> provider11, Provider<DeviceConfigProxy> provider12, Provider<NavigationModeController> provider13) {
        this.contextProvider = provider;
        this.falsingManagerProvider = provider2;
        this.lockPatternUtilsProvider = provider3;
        this.broadcastDispatcherProvider = provider4;
        this.statusBarKeyguardViewManagerLazyProvider = provider5;
        this.dismissCallbackRegistryProvider = provider6;
        this.updateMonitorProvider = provider7;
        this.dumpManagerProvider = provider8;
        this.powerManagerProvider = provider9;
        this.trustManagerProvider = provider10;
        this.uiBgExecutorProvider = provider11;
        this.deviceConfigProvider = provider12;
        this.navigationModeControllerProvider = provider13;
    }

    @Override // javax.inject.Provider
    public KeyguardViewMediator get() {
        return provideInstance(this.contextProvider, this.falsingManagerProvider, this.lockPatternUtilsProvider, this.broadcastDispatcherProvider, this.statusBarKeyguardViewManagerLazyProvider, this.dismissCallbackRegistryProvider, this.updateMonitorProvider, this.dumpManagerProvider, this.powerManagerProvider, this.trustManagerProvider, this.uiBgExecutorProvider, this.deviceConfigProvider, this.navigationModeControllerProvider);
    }

    public static KeyguardViewMediator provideInstance(Provider<Context> provider, Provider<FalsingManager> provider2, Provider<LockPatternUtils> provider3, Provider<BroadcastDispatcher> provider4, Provider<KeyguardViewController> provider5, Provider<DismissCallbackRegistry> provider6, Provider<KeyguardUpdateMonitor> provider7, Provider<DumpManager> provider8, Provider<PowerManager> provider9, Provider<TrustManager> provider10, Provider<Executor> provider11, Provider<DeviceConfigProxy> provider12, Provider<NavigationModeController> provider13) {
        return proxyNewKeyguardViewMediator(provider.get(), provider2.get(), provider3.get(), provider4.get(), DoubleCheck.lazy(provider5), provider6.get(), provider7.get(), provider8.get(), provider9.get(), provider10.get(), provider11.get(), provider12.get(), provider13.get());
    }

    public static KeyguardModule_NewKeyguardViewMediatorFactory create(Provider<Context> provider, Provider<FalsingManager> provider2, Provider<LockPatternUtils> provider3, Provider<BroadcastDispatcher> provider4, Provider<KeyguardViewController> provider5, Provider<DismissCallbackRegistry> provider6, Provider<KeyguardUpdateMonitor> provider7, Provider<DumpManager> provider8, Provider<PowerManager> provider9, Provider<TrustManager> provider10, Provider<Executor> provider11, Provider<DeviceConfigProxy> provider12, Provider<NavigationModeController> provider13) {
        return new KeyguardModule_NewKeyguardViewMediatorFactory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10, provider11, provider12, provider13);
    }

    public static KeyguardViewMediator proxyNewKeyguardViewMediator(Context context, FalsingManager falsingManager, LockPatternUtils lockPatternUtils, BroadcastDispatcher broadcastDispatcher, Lazy<KeyguardViewController> lazy, DismissCallbackRegistry dismissCallbackRegistry, KeyguardUpdateMonitor keyguardUpdateMonitor, DumpManager dumpManager, PowerManager powerManager, TrustManager trustManager, Executor executor, DeviceConfigProxy deviceConfigProxy, NavigationModeController navigationModeController) {
        KeyguardViewMediator newKeyguardViewMediator = KeyguardModule.newKeyguardViewMediator(context, falsingManager, lockPatternUtils, broadcastDispatcher, lazy, dismissCallbackRegistry, keyguardUpdateMonitor, dumpManager, powerManager, trustManager, executor, deviceConfigProxy, navigationModeController);
        Preconditions.checkNotNull(newKeyguardViewMediator, "Cannot return null from a non-@Nullable @Provides method");
        return newKeyguardViewMediator;
    }
}
