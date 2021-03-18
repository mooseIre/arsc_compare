package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NightModeTile_Factory implements Factory<NightModeTile> {
    private final Provider<QSHost> hostProvider;

    public NightModeTile_Factory(Provider<QSHost> provider) {
        this.hostProvider = provider;
    }

    @Override // javax.inject.Provider
    public NightModeTile get() {
        return provideInstance(this.hostProvider);
    }

    public static NightModeTile provideInstance(Provider<QSHost> provider) {
        return new NightModeTile(provider.get());
    }

    public static NightModeTile_Factory create(Provider<QSHost> provider) {
        return new NightModeTile_Factory(provider);
    }
}
