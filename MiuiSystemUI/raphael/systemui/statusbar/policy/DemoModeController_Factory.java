package com.android.systemui.statusbar.policy;

import com.android.systemui.broadcast.BroadcastDispatcher;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class DemoModeController_Factory implements Factory<DemoModeController> {
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;

    public DemoModeController_Factory(Provider<BroadcastDispatcher> provider) {
        this.broadcastDispatcherProvider = provider;
    }

    @Override // javax.inject.Provider
    public DemoModeController get() {
        return provideInstance(this.broadcastDispatcherProvider);
    }

    public static DemoModeController provideInstance(Provider<BroadcastDispatcher> provider) {
        return new DemoModeController(provider.get());
    }

    public static DemoModeController_Factory create(Provider<BroadcastDispatcher> provider) {
        return new DemoModeController_Factory(provider);
    }
}
