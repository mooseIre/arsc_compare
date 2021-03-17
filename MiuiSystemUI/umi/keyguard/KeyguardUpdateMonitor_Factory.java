package com.android.keyguard;

import android.content.Context;
import android.os.Looper;
import com.android.internal.widget.LockPatternUtils;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.util.RingerModeTracker;
import dagger.internal.Factory;
import java.util.concurrent.Executor;
import javax.inject.Provider;

public final class KeyguardUpdateMonitor_Factory implements Factory<KeyguardUpdateMonitor> {
    private final Provider<Executor> backgroundExecutorProvider;
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<Context> contextProvider;
    private final Provider<DumpManager> dumpManagerProvider;
    private final Provider<LockPatternUtils> lockPatternUtilsProvider;
    private final Provider<Looper> mainLooperProvider;
    private final Provider<RingerModeTracker> ringerModeTrackerProvider;
    private final Provider<StatusBarStateController> statusBarStateControllerProvider;

    public KeyguardUpdateMonitor_Factory(Provider<Context> provider, Provider<Looper> provider2, Provider<BroadcastDispatcher> provider3, Provider<DumpManager> provider4, Provider<RingerModeTracker> provider5, Provider<Executor> provider6, Provider<StatusBarStateController> provider7, Provider<LockPatternUtils> provider8) {
        this.contextProvider = provider;
        this.mainLooperProvider = provider2;
        this.broadcastDispatcherProvider = provider3;
        this.dumpManagerProvider = provider4;
        this.ringerModeTrackerProvider = provider5;
        this.backgroundExecutorProvider = provider6;
        this.statusBarStateControllerProvider = provider7;
        this.lockPatternUtilsProvider = provider8;
    }

    @Override // javax.inject.Provider
    public KeyguardUpdateMonitor get() {
        return provideInstance(this.contextProvider, this.mainLooperProvider, this.broadcastDispatcherProvider, this.dumpManagerProvider, this.ringerModeTrackerProvider, this.backgroundExecutorProvider, this.statusBarStateControllerProvider, this.lockPatternUtilsProvider);
    }

    public static KeyguardUpdateMonitor provideInstance(Provider<Context> provider, Provider<Looper> provider2, Provider<BroadcastDispatcher> provider3, Provider<DumpManager> provider4, Provider<RingerModeTracker> provider5, Provider<Executor> provider6, Provider<StatusBarStateController> provider7, Provider<LockPatternUtils> provider8) {
        return new KeyguardUpdateMonitor(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get());
    }

    public static KeyguardUpdateMonitor_Factory create(Provider<Context> provider, Provider<Looper> provider2, Provider<BroadcastDispatcher> provider3, Provider<DumpManager> provider4, Provider<RingerModeTracker> provider5, Provider<Executor> provider6, Provider<StatusBarStateController> provider7, Provider<LockPatternUtils> provider8) {
        return new KeyguardUpdateMonitor_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8);
    }
}
