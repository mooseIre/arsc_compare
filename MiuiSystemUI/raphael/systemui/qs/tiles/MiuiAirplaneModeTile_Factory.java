package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiAirplaneModeTile_Factory implements Factory<MiuiAirplaneModeTile> {
    private final Provider<QSHost> hostProvider;

    public MiuiAirplaneModeTile_Factory(Provider<QSHost> provider) {
        this.hostProvider = provider;
    }

    @Override // javax.inject.Provider
    public MiuiAirplaneModeTile get() {
        return provideInstance(this.hostProvider);
    }

    public static MiuiAirplaneModeTile provideInstance(Provider<QSHost> provider) {
        return new MiuiAirplaneModeTile(provider.get());
    }

    public static MiuiAirplaneModeTile_Factory create(Provider<QSHost> provider) {
        return new MiuiAirplaneModeTile_Factory(provider);
    }
}
