package com.android.systemui.qs.tiles;

import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.qs.QSHost;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.statusbar.policy.LocationController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class LocationTile_Factory implements Factory<LocationTile> {
    private final Provider<ActivityStarter> activityStarterProvider;
    private final Provider<QSHost> hostProvider;
    private final Provider<KeyguardStateController> keyguardStateControllerProvider;
    private final Provider<LocationController> locationControllerProvider;

    public LocationTile_Factory(Provider<QSHost> provider, Provider<LocationController> provider2, Provider<KeyguardStateController> provider3, Provider<ActivityStarter> provider4) {
        this.hostProvider = provider;
        this.locationControllerProvider = provider2;
        this.keyguardStateControllerProvider = provider3;
        this.activityStarterProvider = provider4;
    }

    @Override // javax.inject.Provider
    public LocationTile get() {
        return provideInstance(this.hostProvider, this.locationControllerProvider, this.keyguardStateControllerProvider, this.activityStarterProvider);
    }

    public static LocationTile provideInstance(Provider<QSHost> provider, Provider<LocationController> provider2, Provider<KeyguardStateController> provider3, Provider<ActivityStarter> provider4) {
        return new LocationTile(provider.get(), provider2.get(), provider3.get(), provider4.get());
    }

    public static LocationTile_Factory create(Provider<QSHost> provider, Provider<LocationController> provider2, Provider<KeyguardStateController> provider3, Provider<ActivityStarter> provider4) {
        return new LocationTile_Factory(provider, provider2, provider3, provider4);
    }
}
