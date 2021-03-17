package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import com.android.systemui.statusbar.policy.ZenModeController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MuteTile_Factory implements Factory<MuteTile> {
    private final Provider<QSHost> hostProvider;
    private final Provider<ZenModeController> zenModeControllerProvider;

    public MuteTile_Factory(Provider<QSHost> provider, Provider<ZenModeController> provider2) {
        this.hostProvider = provider;
        this.zenModeControllerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public MuteTile get() {
        return provideInstance(this.hostProvider, this.zenModeControllerProvider);
    }

    public static MuteTile provideInstance(Provider<QSHost> provider, Provider<ZenModeController> provider2) {
        return new MuteTile(provider.get(), provider2.get());
    }

    public static MuteTile_Factory create(Provider<QSHost> provider, Provider<ZenModeController> provider2) {
        return new MuteTile_Factory(provider, provider2);
    }
}
