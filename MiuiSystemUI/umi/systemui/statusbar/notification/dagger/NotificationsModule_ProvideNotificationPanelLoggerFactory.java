package com.android.systemui.statusbar.notification.dagger;

import com.android.systemui.statusbar.notification.logging.NotificationPanelLogger;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

public final class NotificationsModule_ProvideNotificationPanelLoggerFactory implements Factory<NotificationPanelLogger> {
    private static final NotificationsModule_ProvideNotificationPanelLoggerFactory INSTANCE = new NotificationsModule_ProvideNotificationPanelLoggerFactory();

    @Override // javax.inject.Provider
    public NotificationPanelLogger get() {
        return provideInstance();
    }

    public static NotificationPanelLogger provideInstance() {
        return proxyProvideNotificationPanelLogger();
    }

    public static NotificationsModule_ProvideNotificationPanelLoggerFactory create() {
        return INSTANCE;
    }

    public static NotificationPanelLogger proxyProvideNotificationPanelLogger() {
        NotificationPanelLogger provideNotificationPanelLogger = NotificationsModule.provideNotificationPanelLogger();
        Preconditions.checkNotNull(provideNotificationPanelLogger, "Cannot return null from a non-@Nullable @Provides method");
        return provideNotificationPanelLogger;
    }
}
