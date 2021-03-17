package com.android.systemui.statusbar;

import com.android.internal.statusbar.IStatusBarService;
import dagger.internal.Factory;
import java.util.concurrent.Executor;
import javax.inject.Provider;

public final class NotificationClickNotifier_Factory implements Factory<NotificationClickNotifier> {
    private final Provider<IStatusBarService> barServiceProvider;
    private final Provider<Executor> mainExecutorProvider;

    public NotificationClickNotifier_Factory(Provider<IStatusBarService> provider, Provider<Executor> provider2) {
        this.barServiceProvider = provider;
        this.mainExecutorProvider = provider2;
    }

    @Override // javax.inject.Provider
    public NotificationClickNotifier get() {
        return provideInstance(this.barServiceProvider, this.mainExecutorProvider);
    }

    public static NotificationClickNotifier provideInstance(Provider<IStatusBarService> provider, Provider<Executor> provider2) {
        return new NotificationClickNotifier(provider.get(), provider2.get());
    }

    public static NotificationClickNotifier_Factory create(Provider<IStatusBarService> provider, Provider<Executor> provider2) {
        return new NotificationClickNotifier_Factory(provider, provider2);
    }
}
