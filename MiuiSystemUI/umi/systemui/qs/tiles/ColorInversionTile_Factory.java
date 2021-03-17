package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ColorInversionTile_Factory implements Factory<ColorInversionTile> {
    private final Provider<QSHost> hostProvider;

    public ColorInversionTile_Factory(Provider<QSHost> provider) {
        this.hostProvider = provider;
    }

    @Override // javax.inject.Provider
    public ColorInversionTile get() {
        return provideInstance(this.hostProvider);
    }

    public static ColorInversionTile provideInstance(Provider<QSHost> provider) {
        return new ColorInversionTile(provider.get());
    }

    public static ColorInversionTile_Factory create(Provider<QSHost> provider) {
        return new ColorInversionTile_Factory(provider);
    }
}
