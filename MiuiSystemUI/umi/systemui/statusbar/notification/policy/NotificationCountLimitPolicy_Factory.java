package com.android.systemui.statusbar.notification.policy;

import com.android.systemui.statusbar.notification.NotificationEntryManager;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NotificationCountLimitPolicy_Factory implements Factory<NotificationCountLimitPolicy> {
    private final Provider<NotificationEntryManager> entryManagerProvider;

    public NotificationCountLimitPolicy_Factory(Provider<NotificationEntryManager> provider) {
        this.entryManagerProvider = provider;
    }

    @Override // javax.inject.Provider
    public NotificationCountLimitPolicy get() {
        return provideInstance(this.entryManagerProvider);
    }

    public static NotificationCountLimitPolicy provideInstance(Provider<NotificationEntryManager> provider) {
        return new NotificationCountLimitPolicy(provider.get());
    }

    public static NotificationCountLimitPolicy_Factory create(Provider<NotificationEntryManager> provider) {
        return new NotificationCountLimitPolicy_Factory(provider);
    }
}
