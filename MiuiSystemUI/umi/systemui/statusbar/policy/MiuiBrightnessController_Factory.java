package com.android.systemui.statusbar.policy;

import android.content.Context;
import com.android.systemui.broadcast.BroadcastDispatcher;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiBrightnessController_Factory implements Factory<MiuiBrightnessController> {
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<Context> contextProvider;

    public MiuiBrightnessController_Factory(Provider<Context> provider, Provider<BroadcastDispatcher> provider2) {
        this.contextProvider = provider;
        this.broadcastDispatcherProvider = provider2;
    }

    @Override // javax.inject.Provider
    public MiuiBrightnessController get() {
        return provideInstance(this.contextProvider, this.broadcastDispatcherProvider);
    }

    public static MiuiBrightnessController provideInstance(Provider<Context> provider, Provider<BroadcastDispatcher> provider2) {
        return new MiuiBrightnessController(provider.get(), provider2.get());
    }

    public static MiuiBrightnessController_Factory create(Provider<Context> provider, Provider<BroadcastDispatcher> provider2) {
        return new MiuiBrightnessController_Factory(provider, provider2);
    }
}
