package com.android.systemui.statusbar.notification.dagger;

import android.content.Context;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.row.NotificationBlockingHelperManager;
import com.android.systemui.statusbar.notification.row.NotificationGutsManager;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class NotificationsModule_ProvideNotificationBlockingHelperManagerFactory implements Factory<NotificationBlockingHelperManager> {
    private final Provider<Context> contextProvider;
    private final Provider<MetricsLogger> metricsLoggerProvider;
    private final Provider<NotificationEntryManager> notificationEntryManagerProvider;
    private final Provider<NotificationGutsManager> notificationGutsManagerProvider;

    public NotificationsModule_ProvideNotificationBlockingHelperManagerFactory(Provider<Context> provider, Provider<NotificationGutsManager> provider2, Provider<NotificationEntryManager> provider3, Provider<MetricsLogger> provider4) {
        this.contextProvider = provider;
        this.notificationGutsManagerProvider = provider2;
        this.notificationEntryManagerProvider = provider3;
        this.metricsLoggerProvider = provider4;
    }

    @Override // javax.inject.Provider
    public NotificationBlockingHelperManager get() {
        return provideInstance(this.contextProvider, this.notificationGutsManagerProvider, this.notificationEntryManagerProvider, this.metricsLoggerProvider);
    }

    public static NotificationBlockingHelperManager provideInstance(Provider<Context> provider, Provider<NotificationGutsManager> provider2, Provider<NotificationEntryManager> provider3, Provider<MetricsLogger> provider4) {
        return proxyProvideNotificationBlockingHelperManager(provider.get(), provider2.get(), provider3.get(), provider4.get());
    }

    public static NotificationsModule_ProvideNotificationBlockingHelperManagerFactory create(Provider<Context> provider, Provider<NotificationGutsManager> provider2, Provider<NotificationEntryManager> provider3, Provider<MetricsLogger> provider4) {
        return new NotificationsModule_ProvideNotificationBlockingHelperManagerFactory(provider, provider2, provider3, provider4);
    }

    public static NotificationBlockingHelperManager proxyProvideNotificationBlockingHelperManager(Context context, NotificationGutsManager notificationGutsManager, NotificationEntryManager notificationEntryManager, MetricsLogger metricsLogger) {
        NotificationBlockingHelperManager provideNotificationBlockingHelperManager = NotificationsModule.provideNotificationBlockingHelperManager(context, notificationGutsManager, notificationEntryManager, metricsLogger);
        Preconditions.checkNotNull(provideNotificationBlockingHelperManager, "Cannot return null from a non-@Nullable @Provides method");
        return provideNotificationBlockingHelperManager;
    }
}
