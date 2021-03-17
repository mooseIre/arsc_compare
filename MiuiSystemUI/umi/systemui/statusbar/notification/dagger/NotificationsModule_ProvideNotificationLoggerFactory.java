package com.android.systemui.statusbar.notification.dagger;

import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.NotificationListener;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.logging.NotificationLogger;
import com.android.systemui.statusbar.notification.logging.NotificationPanelLogger;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import java.util.concurrent.Executor;
import javax.inject.Provider;

public final class NotificationsModule_ProvideNotificationLoggerFactory implements Factory<NotificationLogger> {
    private final Provider<NotificationEntryManager> entryManagerProvider;
    private final Provider<NotificationLogger.ExpansionStateLogger> expansionStateLoggerProvider;
    private final Provider<NotificationListener> notificationListenerProvider;
    private final Provider<NotificationPanelLogger> notificationPanelLoggerProvider;
    private final Provider<StatusBarStateController> statusBarStateControllerProvider;
    private final Provider<Executor> uiBgExecutorProvider;

    public NotificationsModule_ProvideNotificationLoggerFactory(Provider<NotificationListener> provider, Provider<Executor> provider2, Provider<NotificationEntryManager> provider3, Provider<StatusBarStateController> provider4, Provider<NotificationLogger.ExpansionStateLogger> provider5, Provider<NotificationPanelLogger> provider6) {
        this.notificationListenerProvider = provider;
        this.uiBgExecutorProvider = provider2;
        this.entryManagerProvider = provider3;
        this.statusBarStateControllerProvider = provider4;
        this.expansionStateLoggerProvider = provider5;
        this.notificationPanelLoggerProvider = provider6;
    }

    @Override // javax.inject.Provider
    public NotificationLogger get() {
        return provideInstance(this.notificationListenerProvider, this.uiBgExecutorProvider, this.entryManagerProvider, this.statusBarStateControllerProvider, this.expansionStateLoggerProvider, this.notificationPanelLoggerProvider);
    }

    public static NotificationLogger provideInstance(Provider<NotificationListener> provider, Provider<Executor> provider2, Provider<NotificationEntryManager> provider3, Provider<StatusBarStateController> provider4, Provider<NotificationLogger.ExpansionStateLogger> provider5, Provider<NotificationPanelLogger> provider6) {
        return proxyProvideNotificationLogger(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get());
    }

    public static NotificationsModule_ProvideNotificationLoggerFactory create(Provider<NotificationListener> provider, Provider<Executor> provider2, Provider<NotificationEntryManager> provider3, Provider<StatusBarStateController> provider4, Provider<NotificationLogger.ExpansionStateLogger> provider5, Provider<NotificationPanelLogger> provider6) {
        return new NotificationsModule_ProvideNotificationLoggerFactory(provider, provider2, provider3, provider4, provider5, provider6);
    }

    public static NotificationLogger proxyProvideNotificationLogger(NotificationListener notificationListener, Executor executor, NotificationEntryManager notificationEntryManager, StatusBarStateController statusBarStateController, NotificationLogger.ExpansionStateLogger expansionStateLogger, NotificationPanelLogger notificationPanelLogger) {
        NotificationLogger provideNotificationLogger = NotificationsModule.provideNotificationLogger(notificationListener, executor, notificationEntryManager, statusBarStateController, expansionStateLogger, notificationPanelLogger);
        Preconditions.checkNotNull(provideNotificationLogger, "Cannot return null from a non-@Nullable @Provides method");
        return provideNotificationLogger;
    }
}
