package com.android.systemui.statusbar.notification.dagger;

import android.os.Handler;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class NotificationsModule_ProvideVisualStabilityManagerFactory implements Factory<VisualStabilityManager> {
    private final Provider<Handler> handlerProvider;
    private final Provider<NotificationEntryManager> notificationEntryManagerProvider;

    public NotificationsModule_ProvideVisualStabilityManagerFactory(Provider<NotificationEntryManager> provider, Provider<Handler> provider2) {
        this.notificationEntryManagerProvider = provider;
        this.handlerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public VisualStabilityManager get() {
        return provideInstance(this.notificationEntryManagerProvider, this.handlerProvider);
    }

    public static VisualStabilityManager provideInstance(Provider<NotificationEntryManager> provider, Provider<Handler> provider2) {
        return proxyProvideVisualStabilityManager(provider.get(), provider2.get());
    }

    public static NotificationsModule_ProvideVisualStabilityManagerFactory create(Provider<NotificationEntryManager> provider, Provider<Handler> provider2) {
        return new NotificationsModule_ProvideVisualStabilityManagerFactory(provider, provider2);
    }

    public static VisualStabilityManager proxyProvideVisualStabilityManager(NotificationEntryManager notificationEntryManager, Handler handler) {
        VisualStabilityManager provideVisualStabilityManager = NotificationsModule.provideVisualStabilityManager(notificationEntryManager, handler);
        Preconditions.checkNotNull(provideVisualStabilityManager, "Cannot return null from a non-@Nullable @Provides method");
        return provideVisualStabilityManager;
    }
}
