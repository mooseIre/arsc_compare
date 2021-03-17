package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class UserTile_Factory implements Factory<UserTile> {
    private final Provider<QSHost> hostProvider;
    private final Provider<UserInfoController> userInfoControllerProvider;
    private final Provider<UserSwitcherController> userSwitcherControllerProvider;

    public UserTile_Factory(Provider<QSHost> provider, Provider<UserSwitcherController> provider2, Provider<UserInfoController> provider3) {
        this.hostProvider = provider;
        this.userSwitcherControllerProvider = provider2;
        this.userInfoControllerProvider = provider3;
    }

    @Override // javax.inject.Provider
    public UserTile get() {
        return provideInstance(this.hostProvider, this.userSwitcherControllerProvider, this.userInfoControllerProvider);
    }

    public static UserTile provideInstance(Provider<QSHost> provider, Provider<UserSwitcherController> provider2, Provider<UserInfoController> provider3) {
        return new UserTile(provider.get(), provider2.get(), provider3.get());
    }

    public static UserTile_Factory create(Provider<QSHost> provider, Provider<UserSwitcherController> provider2, Provider<UserInfoController> provider3) {
        return new UserTile_Factory(provider, provider2, provider3);
    }
}
