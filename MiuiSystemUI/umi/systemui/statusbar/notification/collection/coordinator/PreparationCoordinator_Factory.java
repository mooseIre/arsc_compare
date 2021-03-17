package com.android.systemui.statusbar.notification.collection.coordinator;

import com.android.internal.statusbar.IStatusBarService;
import com.android.systemui.statusbar.notification.collection.NotifInflaterImpl;
import com.android.systemui.statusbar.notification.collection.NotifViewBarn;
import com.android.systemui.statusbar.notification.row.NotifInflationErrorManager;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class PreparationCoordinator_Factory implements Factory<PreparationCoordinator> {
    private final Provider<NotifInflationErrorManager> errorManagerProvider;
    private final Provider<PreparationCoordinatorLogger> loggerProvider;
    private final Provider<NotifInflaterImpl> notifInflaterProvider;
    private final Provider<IStatusBarService> serviceProvider;
    private final Provider<NotifViewBarn> viewBarnProvider;

    public PreparationCoordinator_Factory(Provider<PreparationCoordinatorLogger> provider, Provider<NotifInflaterImpl> provider2, Provider<NotifInflationErrorManager> provider3, Provider<NotifViewBarn> provider4, Provider<IStatusBarService> provider5) {
        this.loggerProvider = provider;
        this.notifInflaterProvider = provider2;
        this.errorManagerProvider = provider3;
        this.viewBarnProvider = provider4;
        this.serviceProvider = provider5;
    }

    @Override // javax.inject.Provider
    public PreparationCoordinator get() {
        return provideInstance(this.loggerProvider, this.notifInflaterProvider, this.errorManagerProvider, this.viewBarnProvider, this.serviceProvider);
    }

    public static PreparationCoordinator provideInstance(Provider<PreparationCoordinatorLogger> provider, Provider<NotifInflaterImpl> provider2, Provider<NotifInflationErrorManager> provider3, Provider<NotifViewBarn> provider4, Provider<IStatusBarService> provider5) {
        return new PreparationCoordinator(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get());
    }

    public static PreparationCoordinator_Factory create(Provider<PreparationCoordinatorLogger> provider, Provider<NotifInflaterImpl> provider2, Provider<NotifInflationErrorManager> provider3, Provider<NotifViewBarn> provider4, Provider<IStatusBarService> provider5) {
        return new PreparationCoordinator_Factory(provider, provider2, provider3, provider4, provider5);
    }
}
