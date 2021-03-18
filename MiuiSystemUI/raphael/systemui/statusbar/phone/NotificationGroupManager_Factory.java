package com.android.systemui.statusbar.phone;

import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.notification.people.PeopleNotificationIdentifier;
import dagger.internal.DoubleCheck;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NotificationGroupManager_Factory implements Factory<NotificationGroupManager> {
    private final Provider<PeopleNotificationIdentifier> peopleNotificationIdentifierProvider;
    private final Provider<StatusBarStateController> statusBarStateControllerProvider;

    public NotificationGroupManager_Factory(Provider<StatusBarStateController> provider, Provider<PeopleNotificationIdentifier> provider2) {
        this.statusBarStateControllerProvider = provider;
        this.peopleNotificationIdentifierProvider = provider2;
    }

    @Override // javax.inject.Provider
    public NotificationGroupManager get() {
        return provideInstance(this.statusBarStateControllerProvider, this.peopleNotificationIdentifierProvider);
    }

    public static NotificationGroupManager provideInstance(Provider<StatusBarStateController> provider, Provider<PeopleNotificationIdentifier> provider2) {
        return new NotificationGroupManager(provider.get(), DoubleCheck.lazy(provider2));
    }

    public static NotificationGroupManager_Factory create(Provider<StatusBarStateController> provider, Provider<PeopleNotificationIdentifier> provider2) {
        return new NotificationGroupManager_Factory(provider, provider2);
    }
}
