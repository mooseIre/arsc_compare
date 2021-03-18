package com.android.systemui.theme;

import android.content.Context;
import android.os.Handler;
import com.android.systemui.broadcast.BroadcastDispatcher;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ThemeOverlayController_Factory implements Factory<ThemeOverlayController> {
    private final Provider<Handler> bgHandlerProvider;
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<Context> contextProvider;

    public ThemeOverlayController_Factory(Provider<Context> provider, Provider<BroadcastDispatcher> provider2, Provider<Handler> provider3) {
        this.contextProvider = provider;
        this.broadcastDispatcherProvider = provider2;
        this.bgHandlerProvider = provider3;
    }

    @Override // javax.inject.Provider
    public ThemeOverlayController get() {
        return provideInstance(this.contextProvider, this.broadcastDispatcherProvider, this.bgHandlerProvider);
    }

    public static ThemeOverlayController provideInstance(Provider<Context> provider, Provider<BroadcastDispatcher> provider2, Provider<Handler> provider3) {
        return new ThemeOverlayController(provider.get(), provider2.get(), provider3.get());
    }

    public static ThemeOverlayController_Factory create(Provider<Context> provider, Provider<BroadcastDispatcher> provider2, Provider<Handler> provider3) {
        return new ThemeOverlayController_Factory(provider, provider2, provider3);
    }
}
