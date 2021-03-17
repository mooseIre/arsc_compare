package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import com.android.systemui.statusbar.policy.ZenModeController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class QuietModeTile_Factory implements Factory<QuietModeTile> {
    private final Provider<QSHost> hostProvider;
    private final Provider<ZenModeController> zenModeControllerProvider;

    public QuietModeTile_Factory(Provider<QSHost> provider, Provider<ZenModeController> provider2) {
        this.hostProvider = provider;
        this.zenModeControllerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public QuietModeTile get() {
        return provideInstance(this.hostProvider, this.zenModeControllerProvider);
    }

    public static QuietModeTile provideInstance(Provider<QSHost> provider, Provider<ZenModeController> provider2) {
        return new QuietModeTile(provider.get(), provider2.get());
    }

    public static QuietModeTile_Factory create(Provider<QSHost> provider, Provider<ZenModeController> provider2) {
        return new QuietModeTile_Factory(provider, provider2);
    }
}
