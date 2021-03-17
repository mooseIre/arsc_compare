package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import com.android.systemui.statusbar.policy.HotspotController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiHotspotTile_Factory implements Factory<MiuiHotspotTile> {
    private final Provider<QSHost> hostProvider;
    private final Provider<HotspotController> hotspotControllerProvider;

    public MiuiHotspotTile_Factory(Provider<QSHost> provider, Provider<HotspotController> provider2) {
        this.hostProvider = provider;
        this.hotspotControllerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public MiuiHotspotTile get() {
        return provideInstance(this.hostProvider, this.hotspotControllerProvider);
    }

    public static MiuiHotspotTile provideInstance(Provider<QSHost> provider, Provider<HotspotController> provider2) {
        return new MiuiHotspotTile(provider.get(), provider2.get());
    }

    public static MiuiHotspotTile_Factory create(Provider<QSHost> provider, Provider<HotspotController> provider2) {
        return new MiuiHotspotTile_Factory(provider, provider2);
    }
}
