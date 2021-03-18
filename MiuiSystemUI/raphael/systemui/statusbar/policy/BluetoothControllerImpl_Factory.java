package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.os.Looper;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class BluetoothControllerImpl_Factory implements Factory<BluetoothControllerImpl> {
    private final Provider<Looper> bgLooperProvider;
    private final Provider<Context> contextProvider;
    private final Provider<LocalBluetoothManager> localBluetoothManagerProvider;
    private final Provider<Looper> mainLooperProvider;

    public BluetoothControllerImpl_Factory(Provider<Context> provider, Provider<Looper> provider2, Provider<Looper> provider3, Provider<LocalBluetoothManager> provider4) {
        this.contextProvider = provider;
        this.bgLooperProvider = provider2;
        this.mainLooperProvider = provider3;
        this.localBluetoothManagerProvider = provider4;
    }

    @Override // javax.inject.Provider
    public BluetoothControllerImpl get() {
        return provideInstance(this.contextProvider, this.bgLooperProvider, this.mainLooperProvider, this.localBluetoothManagerProvider);
    }

    public static BluetoothControllerImpl provideInstance(Provider<Context> provider, Provider<Looper> provider2, Provider<Looper> provider3, Provider<LocalBluetoothManager> provider4) {
        return new BluetoothControllerImpl(provider.get(), provider2.get(), provider3.get(), provider4.get());
    }

    public static BluetoothControllerImpl_Factory create(Provider<Context> provider, Provider<Looper> provider2, Provider<Looper> provider3, Provider<LocalBluetoothManager> provider4) {
        return new BluetoothControllerImpl_Factory(provider, provider2, provider3, provider4);
    }
}
