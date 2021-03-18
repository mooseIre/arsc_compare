package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ScreenLockTile_Factory implements Factory<ScreenLockTile> {
    private final Provider<QSHost> hostProvider;

    public ScreenLockTile_Factory(Provider<QSHost> provider) {
        this.hostProvider = provider;
    }

    @Override // javax.inject.Provider
    public ScreenLockTile get() {
        return provideInstance(this.hostProvider);
    }

    public static ScreenLockTile provideInstance(Provider<QSHost> provider) {
        return new ScreenLockTile(provider.get());
    }

    public static ScreenLockTile_Factory create(Provider<QSHost> provider) {
        return new ScreenLockTile_Factory(provider);
    }
}
