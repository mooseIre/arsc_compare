package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import com.android.systemui.statusbar.policy.FlashlightController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class FlashlightTile_Factory implements Factory<FlashlightTile> {
    private final Provider<FlashlightController> flashlightControllerProvider;
    private final Provider<QSHost> hostProvider;

    public FlashlightTile_Factory(Provider<QSHost> provider, Provider<FlashlightController> provider2) {
        this.hostProvider = provider;
        this.flashlightControllerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public FlashlightTile get() {
        return provideInstance(this.hostProvider, this.flashlightControllerProvider);
    }

    public static FlashlightTile provideInstance(Provider<QSHost> provider, Provider<FlashlightController> provider2) {
        return new FlashlightTile(provider.get(), provider2.get());
    }

    public static FlashlightTile_Factory create(Provider<QSHost> provider, Provider<FlashlightController> provider2) {
        return new FlashlightTile_Factory(provider, provider2);
    }
}
