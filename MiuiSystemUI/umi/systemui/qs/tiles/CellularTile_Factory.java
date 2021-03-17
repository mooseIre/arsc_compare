package com.android.systemui.qs.tiles;

import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.qs.QSHost;
import com.android.systemui.statusbar.policy.NetworkController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class CellularTile_Factory implements Factory<CellularTile> {
    private final Provider<ActivityStarter> activityStarterProvider;
    private final Provider<QSHost> hostProvider;
    private final Provider<NetworkController> networkControllerProvider;

    public CellularTile_Factory(Provider<QSHost> provider, Provider<NetworkController> provider2, Provider<ActivityStarter> provider3) {
        this.hostProvider = provider;
        this.networkControllerProvider = provider2;
        this.activityStarterProvider = provider3;
    }

    @Override // javax.inject.Provider
    public CellularTile get() {
        return provideInstance(this.hostProvider, this.networkControllerProvider, this.activityStarterProvider);
    }

    public static CellularTile provideInstance(Provider<QSHost> provider, Provider<NetworkController> provider2, Provider<ActivityStarter> provider3) {
        return new CellularTile(provider.get(), provider2.get(), provider3.get());
    }

    public static CellularTile_Factory create(Provider<QSHost> provider, Provider<NetworkController> provider2, Provider<ActivityStarter> provider3) {
        return new CellularTile_Factory(provider, provider2, provider3);
    }
}
