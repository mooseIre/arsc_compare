package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ScreenButtonTile_Factory implements Factory<ScreenButtonTile> {
    private final Provider<QSHost> hostProvider;

    public ScreenButtonTile_Factory(Provider<QSHost> provider) {
        this.hostProvider = provider;
    }

    @Override // javax.inject.Provider
    public ScreenButtonTile get() {
        return provideInstance(this.hostProvider);
    }

    public static ScreenButtonTile provideInstance(Provider<QSHost> provider) {
        return new ScreenButtonTile(provider.get());
    }

    public static ScreenButtonTile_Factory create(Provider<QSHost> provider) {
        return new ScreenButtonTile_Factory(provider);
    }
}
