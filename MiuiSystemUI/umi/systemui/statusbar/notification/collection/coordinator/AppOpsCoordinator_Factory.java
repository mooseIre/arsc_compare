package com.android.systemui.statusbar.notification.collection.coordinator;

import com.android.systemui.ForegroundServiceController;
import com.android.systemui.appops.AppOpsController;
import com.android.systemui.util.concurrency.DelayableExecutor;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class AppOpsCoordinator_Factory implements Factory<AppOpsCoordinator> {
    private final Provider<AppOpsController> appOpsControllerProvider;
    private final Provider<ForegroundServiceController> foregroundServiceControllerProvider;
    private final Provider<DelayableExecutor> mainExecutorProvider;

    public AppOpsCoordinator_Factory(Provider<ForegroundServiceController> provider, Provider<AppOpsController> provider2, Provider<DelayableExecutor> provider3) {
        this.foregroundServiceControllerProvider = provider;
        this.appOpsControllerProvider = provider2;
        this.mainExecutorProvider = provider3;
    }

    @Override // javax.inject.Provider
    public AppOpsCoordinator get() {
        return provideInstance(this.foregroundServiceControllerProvider, this.appOpsControllerProvider, this.mainExecutorProvider);
    }

    public static AppOpsCoordinator provideInstance(Provider<ForegroundServiceController> provider, Provider<AppOpsController> provider2, Provider<DelayableExecutor> provider3) {
        return new AppOpsCoordinator(provider.get(), provider2.get(), provider3.get());
    }

    public static AppOpsCoordinator_Factory create(Provider<ForegroundServiceController> provider, Provider<AppOpsController> provider2, Provider<DelayableExecutor> provider3) {
        return new AppOpsCoordinator_Factory(provider, provider2, provider3);
    }
}
