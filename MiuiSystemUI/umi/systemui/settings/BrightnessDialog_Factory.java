package com.android.systemui.settings;

import com.android.systemui.broadcast.BroadcastDispatcher;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class BrightnessDialog_Factory implements Factory<BrightnessDialog> {
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;

    public BrightnessDialog_Factory(Provider<BroadcastDispatcher> provider) {
        this.broadcastDispatcherProvider = provider;
    }

    @Override // javax.inject.Provider
    public BrightnessDialog get() {
        return provideInstance(this.broadcastDispatcherProvider);
    }

    public static BrightnessDialog provideInstance(Provider<BroadcastDispatcher> provider) {
        return new BrightnessDialog(provider.get());
    }

    public static BrightnessDialog_Factory create(Provider<BroadcastDispatcher> provider) {
        return new BrightnessDialog_Factory(provider);
    }
}
