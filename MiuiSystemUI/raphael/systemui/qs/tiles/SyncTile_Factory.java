package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class SyncTile_Factory implements Factory<SyncTile> {
    private final Provider<QSHost> hostProvider;

    public SyncTile_Factory(Provider<QSHost> provider) {
        this.hostProvider = provider;
    }

    @Override // javax.inject.Provider
    public SyncTile get() {
        return provideInstance(this.hostProvider);
    }

    public static SyncTile provideInstance(Provider<QSHost> provider) {
        return new SyncTile(provider.get());
    }

    public static SyncTile_Factory create(Provider<QSHost> provider) {
        return new SyncTile_Factory(provider);
    }
}
