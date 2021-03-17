package com.android.systemui.statusbar.notification.people;

import com.android.systemui.statusbar.phone.NotificationGroupManager;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class PeopleNotificationIdentifierImpl_Factory implements Factory<PeopleNotificationIdentifierImpl> {
    private final Provider<NotificationGroupManager> groupManagerProvider;
    private final Provider<NotificationPersonExtractor> personExtractorProvider;

    public PeopleNotificationIdentifierImpl_Factory(Provider<NotificationPersonExtractor> provider, Provider<NotificationGroupManager> provider2) {
        this.personExtractorProvider = provider;
        this.groupManagerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public PeopleNotificationIdentifierImpl get() {
        return provideInstance(this.personExtractorProvider, this.groupManagerProvider);
    }

    public static PeopleNotificationIdentifierImpl provideInstance(Provider<NotificationPersonExtractor> provider, Provider<NotificationGroupManager> provider2) {
        return new PeopleNotificationIdentifierImpl(provider.get(), provider2.get());
    }

    public static PeopleNotificationIdentifierImpl_Factory create(Provider<NotificationPersonExtractor> provider, Provider<NotificationGroupManager> provider2) {
        return new PeopleNotificationIdentifierImpl_Factory(provider, provider2);
    }
}
