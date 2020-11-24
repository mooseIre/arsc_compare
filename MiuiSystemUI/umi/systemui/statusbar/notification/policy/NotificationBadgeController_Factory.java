package com.android.systemui.statusbar.notification.policy;

import android.content.Context;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NotificationBadgeController_Factory implements Factory<NotificationBadgeController> {
    private final Provider<Context> contextProvider;
    private final Provider<NotificationEntryManager> entryManagerProvider;
    private final Provider<NotificationGroupManager> groupManagerProvider;

    public NotificationBadgeController_Factory(Provider<Context> provider, Provider<NotificationEntryManager> provider2, Provider<NotificationGroupManager> provider3) {
        this.contextProvider = provider;
        this.entryManagerProvider = provider2;
        this.groupManagerProvider = provider3;
    }

    public NotificationBadgeController get() {
        return provideInstance(this.contextProvider, this.entryManagerProvider, this.groupManagerProvider);
    }

    public static NotificationBadgeController provideInstance(Provider<Context> provider, Provider<NotificationEntryManager> provider2, Provider<NotificationGroupManager> provider3) {
        return new NotificationBadgeController(provider.get(), provider2.get(), provider3.get());
    }

    public static NotificationBadgeController_Factory create(Provider<Context> provider, Provider<NotificationEntryManager> provider2, Provider<NotificationGroupManager> provider3) {
        return new NotificationBadgeController_Factory(provider, provider2, provider3);
    }
}
