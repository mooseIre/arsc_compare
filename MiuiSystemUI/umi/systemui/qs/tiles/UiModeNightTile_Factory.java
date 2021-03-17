package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.LocationController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class UiModeNightTile_Factory implements Factory<UiModeNightTile> {
    private final Provider<BatteryController> batteryControllerProvider;
    private final Provider<ConfigurationController> configurationControllerProvider;
    private final Provider<QSHost> hostProvider;
    private final Provider<LocationController> locationControllerProvider;

    public UiModeNightTile_Factory(Provider<QSHost> provider, Provider<ConfigurationController> provider2, Provider<BatteryController> provider3, Provider<LocationController> provider4) {
        this.hostProvider = provider;
        this.configurationControllerProvider = provider2;
        this.batteryControllerProvider = provider3;
        this.locationControllerProvider = provider4;
    }

    @Override // javax.inject.Provider
    public UiModeNightTile get() {
        return provideInstance(this.hostProvider, this.configurationControllerProvider, this.batteryControllerProvider, this.locationControllerProvider);
    }

    public static UiModeNightTile provideInstance(Provider<QSHost> provider, Provider<ConfigurationController> provider2, Provider<BatteryController> provider3, Provider<LocationController> provider4) {
        return new UiModeNightTile(provider.get(), provider2.get(), provider3.get(), provider4.get());
    }

    public static UiModeNightTile_Factory create(Provider<QSHost> provider, Provider<ConfigurationController> provider2, Provider<BatteryController> provider3, Provider<LocationController> provider4) {
        return new UiModeNightTile_Factory(provider, provider2, provider3, provider4);
    }
}
