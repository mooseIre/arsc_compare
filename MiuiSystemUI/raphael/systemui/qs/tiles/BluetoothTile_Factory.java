package com.android.systemui.qs.tiles;

import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.qs.QSHost;
import com.android.systemui.statusbar.policy.BluetoothController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class BluetoothTile_Factory implements Factory<BluetoothTile> {
    private final Provider<ActivityStarter> activityStarterProvider;
    private final Provider<BluetoothController> bluetoothControllerProvider;
    private final Provider<QSHost> hostProvider;

    public BluetoothTile_Factory(Provider<QSHost> provider, Provider<BluetoothController> provider2, Provider<ActivityStarter> provider3) {
        this.hostProvider = provider;
        this.bluetoothControllerProvider = provider2;
        this.activityStarterProvider = provider3;
    }

    @Override // javax.inject.Provider
    public BluetoothTile get() {
        return provideInstance(this.hostProvider, this.bluetoothControllerProvider, this.activityStarterProvider);
    }

    public static BluetoothTile provideInstance(Provider<QSHost> provider, Provider<BluetoothController> provider2, Provider<ActivityStarter> provider3) {
        return new BluetoothTile(provider.get(), provider2.get(), provider3.get());
    }

    public static BluetoothTile_Factory create(Provider<QSHost> provider, Provider<BluetoothController> provider2, Provider<ActivityStarter> provider3) {
        return new BluetoothTile_Factory(provider, provider2, provider3);
    }
}
