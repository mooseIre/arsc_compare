package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class PowerSaverTile_Factory implements Factory<PowerSaverTile> {
    private final Provider<QSHost> hostProvider;

    public PowerSaverTile_Factory(Provider<QSHost> provider) {
        this.hostProvider = provider;
    }

    @Override // javax.inject.Provider
    public PowerSaverTile get() {
        return provideInstance(this.hostProvider);
    }

    public static PowerSaverTile provideInstance(Provider<QSHost> provider) {
        return new PowerSaverTile(provider.get());
    }

    public static PowerSaverTile_Factory create(Provider<QSHost> provider) {
        return new PowerSaverTile_Factory(provider);
    }
}
