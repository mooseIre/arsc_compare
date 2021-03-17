package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import com.android.systemui.statusbar.policy.ZenModeController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class VibrateTile_Factory implements Factory<VibrateTile> {
    private final Provider<QSHost> hostProvider;
    private final Provider<ZenModeController> zenModeControllerProvider;

    public VibrateTile_Factory(Provider<QSHost> provider, Provider<ZenModeController> provider2) {
        this.hostProvider = provider;
        this.zenModeControllerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public VibrateTile get() {
        return provideInstance(this.hostProvider, this.zenModeControllerProvider);
    }

    public static VibrateTile provideInstance(Provider<QSHost> provider, Provider<ZenModeController> provider2) {
        return new VibrateTile(provider.get(), provider2.get());
    }

    public static VibrateTile_Factory create(Provider<QSHost> provider, Provider<ZenModeController> provider2) {
        return new VibrateTile_Factory(provider, provider2);
    }
}
