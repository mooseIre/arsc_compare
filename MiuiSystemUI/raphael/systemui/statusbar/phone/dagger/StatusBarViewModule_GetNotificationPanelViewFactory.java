package com.android.systemui.statusbar.phone.dagger;

import com.android.systemui.statusbar.phone.NotificationPanelView;
import com.android.systemui.statusbar.phone.NotificationShadeWindowView;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class StatusBarViewModule_GetNotificationPanelViewFactory implements Factory<NotificationPanelView> {
    private final Provider<NotificationShadeWindowView> notificationShadeWindowViewProvider;

    public StatusBarViewModule_GetNotificationPanelViewFactory(Provider<NotificationShadeWindowView> provider) {
        this.notificationShadeWindowViewProvider = provider;
    }

    @Override // javax.inject.Provider
    public NotificationPanelView get() {
        return provideInstance(this.notificationShadeWindowViewProvider);
    }

    public static NotificationPanelView provideInstance(Provider<NotificationShadeWindowView> provider) {
        return proxyGetNotificationPanelView(provider.get());
    }

    public static StatusBarViewModule_GetNotificationPanelViewFactory create(Provider<NotificationShadeWindowView> provider) {
        return new StatusBarViewModule_GetNotificationPanelViewFactory(provider);
    }

    public static NotificationPanelView proxyGetNotificationPanelView(NotificationShadeWindowView notificationShadeWindowView) {
        NotificationPanelView notificationPanelView = StatusBarViewModule.getNotificationPanelView(notificationShadeWindowView);
        Preconditions.checkNotNull(notificationPanelView, "Cannot return null from a non-@Nullable @Provides method");
        return notificationPanelView;
    }
}
