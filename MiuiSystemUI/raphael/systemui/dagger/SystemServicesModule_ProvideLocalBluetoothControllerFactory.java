package com.android.systemui.dagger;

import android.content.Context;
import android.os.Handler;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class SystemServicesModule_ProvideLocalBluetoothControllerFactory implements Factory<LocalBluetoothManager> {
    private final Provider<Handler> bgHandlerProvider;
    private final Provider<Context> contextProvider;

    public SystemServicesModule_ProvideLocalBluetoothControllerFactory(Provider<Context> provider, Provider<Handler> provider2) {
        this.contextProvider = provider;
        this.bgHandlerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public LocalBluetoothManager get() {
        return provideInstance(this.contextProvider, this.bgHandlerProvider);
    }

    public static LocalBluetoothManager provideInstance(Provider<Context> provider, Provider<Handler> provider2) {
        return proxyProvideLocalBluetoothController(provider.get(), provider2.get());
    }

    public static SystemServicesModule_ProvideLocalBluetoothControllerFactory create(Provider<Context> provider, Provider<Handler> provider2) {
        return new SystemServicesModule_ProvideLocalBluetoothControllerFactory(provider, provider2);
    }

    public static LocalBluetoothManager proxyProvideLocalBluetoothController(Context context, Handler handler) {
        return SystemServicesModule.provideLocalBluetoothController(context, handler);
    }
}
