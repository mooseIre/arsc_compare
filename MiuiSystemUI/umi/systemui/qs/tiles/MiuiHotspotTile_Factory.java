package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import com.android.systemui.statusbar.policy.HotspotController;
import com.android.systemui.statusbar.policy.NetworkController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiHotspotTile_Factory implements Factory<MiuiHotspotTile> {
    private final Provider<QSHost> hostProvider;
    private final Provider<HotspotController> hotspotControllerProvider;
    private final Provider<NetworkController> networkControllerProvider;

    public MiuiHotspotTile_Factory(Provider<QSHost> provider, Provider<HotspotController> provider2, Provider<NetworkController> provider3) {
        this.hostProvider = provider;
        this.hotspotControllerProvider = provider2;
        this.networkControllerProvider = provider3;
    }

    @Override // javax.inject.Provider
    public MiuiHotspotTile get() {
        return provideInstance(this.hostProvider, this.hotspotControllerProvider, this.networkControllerProvider);
    }

    public static MiuiHotspotTile provideInstance(Provider<QSHost> provider, Provider<HotspotController> provider2, Provider<NetworkController> provider3) {
        return new MiuiHotspotTile(provider.get(), provider2.get(), provider3.get());
    }

    public static MiuiHotspotTile_Factory create(Provider<QSHost> provider, Provider<HotspotController> provider2, Provider<NetworkController> provider3) {
        return new MiuiHotspotTile_Factory(provider, provider2, provider3);
    }
}
