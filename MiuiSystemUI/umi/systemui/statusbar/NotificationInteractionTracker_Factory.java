package com.android.systemui.statusbar;

import com.android.systemui.statusbar.notification.NotificationEntryManager;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NotificationInteractionTracker_Factory implements Factory<NotificationInteractionTracker> {
    private final Provider<NotificationClickNotifier> clickerProvider;
    private final Provider<NotificationEntryManager> entryManagerProvider;

    public NotificationInteractionTracker_Factory(Provider<NotificationClickNotifier> provider, Provider<NotificationEntryManager> provider2) {
        this.clickerProvider = provider;
        this.entryManagerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public NotificationInteractionTracker get() {
        return provideInstance(this.clickerProvider, this.entryManagerProvider);
    }

    public static NotificationInteractionTracker provideInstance(Provider<NotificationClickNotifier> provider, Provider<NotificationEntryManager> provider2) {
        return new NotificationInteractionTracker(provider.get(), provider2.get());
    }

    public static NotificationInteractionTracker_Factory create(Provider<NotificationClickNotifier> provider, Provider<NotificationEntryManager> provider2) {
        return new NotificationInteractionTracker_Factory(provider, provider2);
    }
}
