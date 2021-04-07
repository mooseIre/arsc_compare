package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import com.android.systemui.statusbar.policy.NetworkController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiAirplaneModeTile_Factory implements Factory<MiuiAirplaneModeTile> {
    private final Provider<QSHost> hostProvider;
    private final Provider<NetworkController> networkControllerProvider;

    public MiuiAirplaneModeTile_Factory(Provider<QSHost> provider, Provider<NetworkController> provider2) {
        this.hostProvider = provider;
        this.networkControllerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public MiuiAirplaneModeTile get() {
        return provideInstance(this.hostProvider, this.networkControllerProvider);
    }

    public static MiuiAirplaneModeTile provideInstance(Provider<QSHost> provider, Provider<NetworkController> provider2) {
        return new MiuiAirplaneModeTile(provider.get(), provider2.get());
    }

    public static MiuiAirplaneModeTile_Factory create(Provider<QSHost> provider, Provider<NetworkController> provider2) {
        return new MiuiAirplaneModeTile_Factory(provider, provider2);
    }
}
