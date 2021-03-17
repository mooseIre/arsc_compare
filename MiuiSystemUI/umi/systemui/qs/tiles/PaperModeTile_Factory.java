package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import com.android.systemui.statusbar.policy.PaperModeController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class PaperModeTile_Factory implements Factory<PaperModeTile> {
    private final Provider<QSHost> hostProvider;
    private final Provider<PaperModeController> paperModeControllerProvider;

    public PaperModeTile_Factory(Provider<QSHost> provider, Provider<PaperModeController> provider2) {
        this.hostProvider = provider;
        this.paperModeControllerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public PaperModeTile get() {
        return provideInstance(this.hostProvider, this.paperModeControllerProvider);
    }

    public static PaperModeTile provideInstance(Provider<QSHost> provider, Provider<PaperModeController> provider2) {
        return new PaperModeTile(provider.get(), provider2.get());
    }

    public static PaperModeTile_Factory create(Provider<QSHost> provider, Provider<PaperModeController> provider2) {
        return new PaperModeTile_Factory(provider, provider2);
    }
}
