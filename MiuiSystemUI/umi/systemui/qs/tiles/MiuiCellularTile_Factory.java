package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import com.android.systemui.statusbar.policy.NetworkController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiCellularTile_Factory implements Factory<MiuiCellularTile> {
    private final Provider<QSHost> hostProvider;
    private final Provider<NetworkController> networkControllerProvider;

    public MiuiCellularTile_Factory(Provider<QSHost> provider, Provider<NetworkController> provider2) {
        this.hostProvider = provider;
        this.networkControllerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public MiuiCellularTile get() {
        return provideInstance(this.hostProvider, this.networkControllerProvider);
    }

    public static MiuiCellularTile provideInstance(Provider<QSHost> provider, Provider<NetworkController> provider2) {
        return new MiuiCellularTile(provider.get(), provider2.get());
    }

    public static MiuiCellularTile_Factory create(Provider<QSHost> provider, Provider<NetworkController> provider2) {
        return new MiuiCellularTile_Factory(provider, provider2);
    }
}
