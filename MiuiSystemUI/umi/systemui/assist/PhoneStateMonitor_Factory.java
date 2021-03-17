package com.android.systemui.assist;

import android.content.Context;
import com.android.systemui.BootCompleteCache;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.statusbar.phone.StatusBar;
import dagger.Lazy;
import dagger.internal.Factory;
import java.util.Optional;
import javax.inject.Provider;

public final class PhoneStateMonitor_Factory implements Factory<PhoneStateMonitor> {
    private final Provider<BootCompleteCache> bootCompleteCacheProvider;
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<Context> contextProvider;
    private final Provider<Optional<Lazy<StatusBar>>> statusBarOptionalLazyProvider;

    public PhoneStateMonitor_Factory(Provider<Context> provider, Provider<BroadcastDispatcher> provider2, Provider<Optional<Lazy<StatusBar>>> provider3, Provider<BootCompleteCache> provider4) {
        this.contextProvider = provider;
        this.broadcastDispatcherProvider = provider2;
        this.statusBarOptionalLazyProvider = provider3;
        this.bootCompleteCacheProvider = provider4;
    }

    @Override // javax.inject.Provider
    public PhoneStateMonitor get() {
        return provideInstance(this.contextProvider, this.broadcastDispatcherProvider, this.statusBarOptionalLazyProvider, this.bootCompleteCacheProvider);
    }

    public static PhoneStateMonitor provideInstance(Provider<Context> provider, Provider<BroadcastDispatcher> provider2, Provider<Optional<Lazy<StatusBar>>> provider3, Provider<BootCompleteCache> provider4) {
        return new PhoneStateMonitor(provider.get(), provider2.get(), provider3.get(), provider4.get());
    }

    public static PhoneStateMonitor_Factory create(Provider<Context> provider, Provider<BroadcastDispatcher> provider2, Provider<Optional<Lazy<StatusBar>>> provider3, Provider<BootCompleteCache> provider4) {
        return new PhoneStateMonitor_Factory(provider, provider2, provider3, provider4);
    }
}
