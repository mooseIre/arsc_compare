package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class PowerModeTile_Factory implements Factory<PowerModeTile> {
    private final Provider<QSHost> hostProvider;

    public PowerModeTile_Factory(Provider<QSHost> provider) {
        this.hostProvider = provider;
    }

    @Override // javax.inject.Provider
    public PowerModeTile get() {
        return provideInstance(this.hostProvider);
    }

    public static PowerModeTile provideInstance(Provider<QSHost> provider) {
        return new PowerModeTile(provider.get());
    }

    public static PowerModeTile_Factory create(Provider<QSHost> provider) {
        return new PowerModeTile_Factory(provider);
    }
}
