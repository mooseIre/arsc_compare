package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class AutoBrightnessTile_Factory implements Factory<AutoBrightnessTile> {
    private final Provider<QSHost> hostProvider;

    public AutoBrightnessTile_Factory(Provider<QSHost> provider) {
        this.hostProvider = provider;
    }

    @Override // javax.inject.Provider
    public AutoBrightnessTile get() {
        return provideInstance(this.hostProvider);
    }

    public static AutoBrightnessTile provideInstance(Provider<QSHost> provider) {
        return new AutoBrightnessTile(provider.get());
    }

    public static AutoBrightnessTile_Factory create(Provider<QSHost> provider) {
        return new AutoBrightnessTile_Factory(provider);
    }
}
