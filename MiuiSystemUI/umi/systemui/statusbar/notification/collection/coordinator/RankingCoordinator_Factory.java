package com.android.systemui.statusbar.notification.collection.coordinator;

import com.android.systemui.plugins.statusbar.StatusBarStateController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class RankingCoordinator_Factory implements Factory<RankingCoordinator> {
    private final Provider<StatusBarStateController> statusBarStateControllerProvider;

    public RankingCoordinator_Factory(Provider<StatusBarStateController> provider) {
        this.statusBarStateControllerProvider = provider;
    }

    @Override // javax.inject.Provider
    public RankingCoordinator get() {
        return provideInstance(this.statusBarStateControllerProvider);
    }

    public static RankingCoordinator provideInstance(Provider<StatusBarStateController> provider) {
        return new RankingCoordinator(provider.get());
    }

    public static RankingCoordinator_Factory create(Provider<StatusBarStateController> provider) {
        return new RankingCoordinator_Factory(provider);
    }
}
