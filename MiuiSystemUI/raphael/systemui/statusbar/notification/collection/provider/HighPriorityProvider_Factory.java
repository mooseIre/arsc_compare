package com.android.systemui.statusbar.notification.collection.provider;

import com.android.systemui.statusbar.notification.people.PeopleNotificationIdentifier;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class HighPriorityProvider_Factory implements Factory<HighPriorityProvider> {
    private final Provider<NotificationGroupManager> groupManagerProvider;
    private final Provider<PeopleNotificationIdentifier> peopleNotificationIdentifierProvider;

    public HighPriorityProvider_Factory(Provider<PeopleNotificationIdentifier> provider, Provider<NotificationGroupManager> provider2) {
        this.peopleNotificationIdentifierProvider = provider;
        this.groupManagerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public HighPriorityProvider get() {
        return provideInstance(this.peopleNotificationIdentifierProvider, this.groupManagerProvider);
    }

    public static HighPriorityProvider provideInstance(Provider<PeopleNotificationIdentifier> provider, Provider<NotificationGroupManager> provider2) {
        return new HighPriorityProvider(provider.get(), provider2.get());
    }

    public static HighPriorityProvider_Factory create(Provider<PeopleNotificationIdentifier> provider, Provider<NotificationGroupManager> provider2) {
        return new HighPriorityProvider_Factory(provider, provider2);
    }
}
