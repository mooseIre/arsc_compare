package com.android.systemui.statusbar.notification.init;

import com.android.systemui.statusbar.NotificationListener;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NotificationsControllerStub_Factory implements Factory<NotificationsControllerStub> {
    private final Provider<NotificationListener> notificationListenerProvider;

    public NotificationsControllerStub_Factory(Provider<NotificationListener> provider) {
        this.notificationListenerProvider = provider;
    }

    @Override // javax.inject.Provider
    public NotificationsControllerStub get() {
        return provideInstance(this.notificationListenerProvider);
    }

    public static NotificationsControllerStub provideInstance(Provider<NotificationListener> provider) {
        return new NotificationsControllerStub(provider.get());
    }

    public static NotificationsControllerStub_Factory create(Provider<NotificationListener> provider) {
        return new NotificationsControllerStub_Factory(provider);
    }
}
