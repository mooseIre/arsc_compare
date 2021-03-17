package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import com.android.systemui.statusbar.policy.BatteryController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class BatterySaverTile_Factory implements Factory<BatterySaverTile> {
    private final Provider<BatteryController> batteryControllerProvider;
    private final Provider<QSHost> hostProvider;

    public BatterySaverTile_Factory(Provider<QSHost> provider, Provider<BatteryController> provider2) {
        this.hostProvider = provider;
        this.batteryControllerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public BatterySaverTile get() {
        return provideInstance(this.hostProvider, this.batteryControllerProvider);
    }

    public static BatterySaverTile provideInstance(Provider<QSHost> provider, Provider<BatteryController> provider2) {
        return new BatterySaverTile(provider.get(), provider2.get());
    }

    public static BatterySaverTile_Factory create(Provider<QSHost> provider, Provider<BatteryController> provider2) {
        return new BatterySaverTile_Factory(provider, provider2);
    }
}
