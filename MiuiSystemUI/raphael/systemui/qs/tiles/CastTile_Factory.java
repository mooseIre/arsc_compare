package com.android.systemui.qs.tiles;

import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.qs.QSHost;
import com.android.systemui.statusbar.policy.CastController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.statusbar.policy.NetworkController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class CastTile_Factory implements Factory<CastTile> {
    private final Provider<ActivityStarter> activityStarterProvider;
    private final Provider<CastController> castControllerProvider;
    private final Provider<QSHost> hostProvider;
    private final Provider<KeyguardStateController> keyguardStateControllerProvider;
    private final Provider<NetworkController> networkControllerProvider;

    public CastTile_Factory(Provider<QSHost> provider, Provider<CastController> provider2, Provider<KeyguardStateController> provider3, Provider<NetworkController> provider4, Provider<ActivityStarter> provider5) {
        this.hostProvider = provider;
        this.castControllerProvider = provider2;
        this.keyguardStateControllerProvider = provider3;
        this.networkControllerProvider = provider4;
        this.activityStarterProvider = provider5;
    }

    @Override // javax.inject.Provider
    public CastTile get() {
        return provideInstance(this.hostProvider, this.castControllerProvider, this.keyguardStateControllerProvider, this.networkControllerProvider, this.activityStarterProvider);
    }

    public static CastTile provideInstance(Provider<QSHost> provider, Provider<CastController> provider2, Provider<KeyguardStateController> provider3, Provider<NetworkController> provider4, Provider<ActivityStarter> provider5) {
        return new CastTile(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get());
    }

    public static CastTile_Factory create(Provider<QSHost> provider, Provider<CastController> provider2, Provider<KeyguardStateController> provider3, Provider<NetworkController> provider4, Provider<ActivityStarter> provider5) {
        return new CastTile_Factory(provider, provider2, provider3, provider4, provider5);
    }
}
