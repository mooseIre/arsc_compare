package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import com.android.systemui.statusbar.policy.DriveModeController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class DriveModeTile_Factory implements Factory<DriveModeTile> {
    private final Provider<DriveModeController> driveModeControllerProvider;
    private final Provider<QSHost> hostProvider;

    public DriveModeTile_Factory(Provider<QSHost> provider, Provider<DriveModeController> provider2) {
        this.hostProvider = provider;
        this.driveModeControllerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public DriveModeTile get() {
        return provideInstance(this.hostProvider, this.driveModeControllerProvider);
    }

    public static DriveModeTile provideInstance(Provider<QSHost> provider, Provider<DriveModeController> provider2) {
        return new DriveModeTile(provider.get(), provider2.get());
    }

    public static DriveModeTile_Factory create(Provider<QSHost> provider, Provider<DriveModeController> provider2) {
        return new DriveModeTile_Factory(provider, provider2);
    }
}
