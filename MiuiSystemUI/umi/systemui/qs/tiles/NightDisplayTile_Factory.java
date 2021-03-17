package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import com.android.systemui.statusbar.policy.LocationController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NightDisplayTile_Factory implements Factory<NightDisplayTile> {
    private final Provider<QSHost> hostProvider;
    private final Provider<LocationController> locationControllerProvider;

    public NightDisplayTile_Factory(Provider<QSHost> provider, Provider<LocationController> provider2) {
        this.hostProvider = provider;
        this.locationControllerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public NightDisplayTile get() {
        return provideInstance(this.hostProvider, this.locationControllerProvider);
    }

    public static NightDisplayTile provideInstance(Provider<QSHost> provider, Provider<LocationController> provider2) {
        return new NightDisplayTile(provider.get(), provider2.get());
    }

    public static NightDisplayTile_Factory create(Provider<QSHost> provider, Provider<LocationController> provider2) {
        return new NightDisplayTile_Factory(provider, provider2);
    }
}
