package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ScreenShotTile_Factory implements Factory<ScreenShotTile> {
    private final Provider<QSHost> hostProvider;

    public ScreenShotTile_Factory(Provider<QSHost> provider) {
        this.hostProvider = provider;
    }

    @Override // javax.inject.Provider
    public ScreenShotTile get() {
        return provideInstance(this.hostProvider);
    }

    public static ScreenShotTile provideInstance(Provider<QSHost> provider) {
        return new ScreenShotTile(provider.get());
    }

    public static ScreenShotTile_Factory create(Provider<QSHost> provider) {
        return new ScreenShotTile_Factory(provider);
    }
}
