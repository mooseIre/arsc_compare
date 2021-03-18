package com.android.keyguard.injector;

import android.content.Context;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class KeyguardViewMediatorInjector_Factory implements Factory<KeyguardViewMediatorInjector> {
    private final Provider<BroadcastDispatcher> mBroadcastDispatcherProvider;
    private final Provider<Context> mContextProvider;
    private final Provider<StatusBarKeyguardViewManager> mStatusBarKeyguardViewManagerProvider;

    public KeyguardViewMediatorInjector_Factory(Provider<Context> provider, Provider<BroadcastDispatcher> provider2, Provider<StatusBarKeyguardViewManager> provider3) {
        this.mContextProvider = provider;
        this.mBroadcastDispatcherProvider = provider2;
        this.mStatusBarKeyguardViewManagerProvider = provider3;
    }

    @Override // javax.inject.Provider
    public KeyguardViewMediatorInjector get() {
        return provideInstance(this.mContextProvider, this.mBroadcastDispatcherProvider, this.mStatusBarKeyguardViewManagerProvider);
    }

    public static KeyguardViewMediatorInjector provideInstance(Provider<Context> provider, Provider<BroadcastDispatcher> provider2, Provider<StatusBarKeyguardViewManager> provider3) {
        return new KeyguardViewMediatorInjector(provider.get(), provider2.get(), provider3.get());
    }

    public static KeyguardViewMediatorInjector_Factory create(Provider<Context> provider, Provider<BroadcastDispatcher> provider2, Provider<StatusBarKeyguardViewManager> provider3) {
        return new KeyguardViewMediatorInjector_Factory(provider, provider2, provider3);
    }
}
