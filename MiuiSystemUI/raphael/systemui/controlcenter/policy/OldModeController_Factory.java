package com.android.systemui.controlcenter.policy;

import com.android.systemui.broadcast.BroadcastDispatcher;
import com.miui.systemui.SettingsObserver;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class OldModeController_Factory implements Factory<OldModeController> {
    private final Provider<BroadcastDispatcher> mBroadcastDispatcherProvider;
    private final Provider<SettingsObserver> mSettingsObserverProvider;

    public OldModeController_Factory(Provider<BroadcastDispatcher> provider, Provider<SettingsObserver> provider2) {
        this.mBroadcastDispatcherProvider = provider;
        this.mSettingsObserverProvider = provider2;
    }

    @Override // javax.inject.Provider
    public OldModeController get() {
        return provideInstance(this.mBroadcastDispatcherProvider, this.mSettingsObserverProvider);
    }

    public static OldModeController provideInstance(Provider<BroadcastDispatcher> provider, Provider<SettingsObserver> provider2) {
        return new OldModeController(provider.get(), provider2.get());
    }

    public static OldModeController_Factory create(Provider<BroadcastDispatcher> provider, Provider<SettingsObserver> provider2) {
        return new OldModeController_Factory(provider, provider2);
    }
}
