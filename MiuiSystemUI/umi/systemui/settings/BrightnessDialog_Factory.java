package com.android.systemui.settings;

import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.statusbar.policy.MiuiBrightnessController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class BrightnessDialog_Factory implements Factory<BrightnessDialog> {
    private final Provider<MiuiBrightnessController> brightnessControllerProvider;
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;

    public BrightnessDialog_Factory(Provider<BroadcastDispatcher> provider, Provider<MiuiBrightnessController> provider2) {
        this.broadcastDispatcherProvider = provider;
        this.brightnessControllerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public BrightnessDialog get() {
        return provideInstance(this.broadcastDispatcherProvider, this.brightnessControllerProvider);
    }

    public static BrightnessDialog provideInstance(Provider<BroadcastDispatcher> provider, Provider<MiuiBrightnessController> provider2) {
        return new BrightnessDialog(provider.get(), provider2.get());
    }

    public static BrightnessDialog_Factory create(Provider<BroadcastDispatcher> provider, Provider<MiuiBrightnessController> provider2) {
        return new BrightnessDialog_Factory(provider, provider2);
    }
}
