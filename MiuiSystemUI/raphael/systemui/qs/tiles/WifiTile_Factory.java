package com.android.systemui.qs.tiles;

import com.android.systemui.controlcenter.policy.SlaveWifiHelper;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.qs.QSHost;
import com.android.systemui.statusbar.policy.NetworkController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class WifiTile_Factory implements Factory<WifiTile> {
    private final Provider<ActivityStarter> activityStarterProvider;
    private final Provider<QSHost> hostProvider;
    private final Provider<NetworkController> networkControllerProvider;
    private final Provider<SlaveWifiHelper> slaveWifiHelperProvider;

    public WifiTile_Factory(Provider<QSHost> provider, Provider<NetworkController> provider2, Provider<ActivityStarter> provider3, Provider<SlaveWifiHelper> provider4) {
        this.hostProvider = provider;
        this.networkControllerProvider = provider2;
        this.activityStarterProvider = provider3;
        this.slaveWifiHelperProvider = provider4;
    }

    @Override // javax.inject.Provider
    public WifiTile get() {
        return provideInstance(this.hostProvider, this.networkControllerProvider, this.activityStarterProvider, this.slaveWifiHelperProvider);
    }

    public static WifiTile provideInstance(Provider<QSHost> provider, Provider<NetworkController> provider2, Provider<ActivityStarter> provider3, Provider<SlaveWifiHelper> provider4) {
        return new WifiTile(provider.get(), provider2.get(), provider3.get(), provider4.get());
    }

    public static WifiTile_Factory create(Provider<QSHost> provider, Provider<NetworkController> provider2, Provider<ActivityStarter> provider3, Provider<SlaveWifiHelper> provider4) {
        return new WifiTile_Factory(provider, provider2, provider3, provider4);
    }
}
