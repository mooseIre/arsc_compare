package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import com.android.systemui.statusbar.phone.ManagedProfileController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class WorkModeTile_Factory implements Factory<WorkModeTile> {
    private final Provider<QSHost> hostProvider;
    private final Provider<ManagedProfileController> managedProfileControllerProvider;

    public WorkModeTile_Factory(Provider<QSHost> provider, Provider<ManagedProfileController> provider2) {
        this.hostProvider = provider;
        this.managedProfileControllerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public WorkModeTile get() {
        return provideInstance(this.hostProvider, this.managedProfileControllerProvider);
    }

    public static WorkModeTile provideInstance(Provider<QSHost> provider, Provider<ManagedProfileController> provider2) {
        return new WorkModeTile(provider.get(), provider2.get());
    }

    public static WorkModeTile_Factory create(Provider<QSHost> provider, Provider<ManagedProfileController> provider2) {
        return new WorkModeTile_Factory(provider, provider2);
    }
}
