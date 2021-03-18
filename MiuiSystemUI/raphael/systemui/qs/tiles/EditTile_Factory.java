package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class EditTile_Factory implements Factory<EditTile> {
    private final Provider<QSHost> hostProvider;

    public EditTile_Factory(Provider<QSHost> provider) {
        this.hostProvider = provider;
    }

    @Override // javax.inject.Provider
    public EditTile get() {
        return provideInstance(this.hostProvider);
    }

    public static EditTile provideInstance(Provider<QSHost> provider) {
        return new EditTile(provider.get());
    }

    public static EditTile_Factory create(Provider<QSHost> provider) {
        return new EditTile_Factory(provider);
    }
}
