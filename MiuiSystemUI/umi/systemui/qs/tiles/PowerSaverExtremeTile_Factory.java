package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class PowerSaverExtremeTile_Factory implements Factory<PowerSaverExtremeTile> {
    private final Provider<QSHost> hostProvider;

    public PowerSaverExtremeTile_Factory(Provider<QSHost> provider) {
        this.hostProvider = provider;
    }

    @Override // javax.inject.Provider
    public PowerSaverExtremeTile get() {
        return provideInstance(this.hostProvider);
    }

    public static PowerSaverExtremeTile provideInstance(Provider<QSHost> provider) {
        return new PowerSaverExtremeTile(provider.get());
    }

    public static PowerSaverExtremeTile_Factory create(Provider<QSHost> provider) {
        return new PowerSaverExtremeTile_Factory(provider);
    }
}
