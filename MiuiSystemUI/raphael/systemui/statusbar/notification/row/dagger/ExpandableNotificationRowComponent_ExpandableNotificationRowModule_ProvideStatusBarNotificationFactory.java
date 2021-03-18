package com.android.systemui.statusbar.notification.row.dagger;

import android.service.notification.StatusBarNotification;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideStatusBarNotificationFactory implements Factory<StatusBarNotification> {
    private final Provider<NotificationEntry> notificationEntryProvider;

    public ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideStatusBarNotificationFactory(Provider<NotificationEntry> provider) {
        this.notificationEntryProvider = provider;
    }

    @Override // javax.inject.Provider
    public StatusBarNotification get() {
        return provideInstance(this.notificationEntryProvider);
    }

    public static StatusBarNotification provideInstance(Provider<NotificationEntry> provider) {
        return proxyProvideStatusBarNotification(provider.get());
    }

    public static ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideStatusBarNotificationFactory create(Provider<NotificationEntry> provider) {
        return new ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideStatusBarNotificationFactory(provider);
    }

    public static StatusBarNotification proxyProvideStatusBarNotification(NotificationEntry notificationEntry) {
        StatusBarNotification provideStatusBarNotification = ExpandableNotificationRowComponent.ExpandableNotificationRowModule.provideStatusBarNotification(notificationEntry);
        Preconditions.checkNotNull(provideStatusBarNotification, "Cannot return null from a non-@Nullable @Provides method");
        return provideStatusBarNotification;
    }
}
