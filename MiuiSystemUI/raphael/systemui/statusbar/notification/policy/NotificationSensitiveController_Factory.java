package com.android.systemui.statusbar.notification.policy;

import android.content.Context;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NotificationSensitiveController_Factory implements Factory<NotificationSensitiveController> {
    private final Provider<Context> contextProvider;
    private final Provider<UserSwitcherController> userSwitcherControllerProvider;

    public NotificationSensitiveController_Factory(Provider<Context> provider, Provider<UserSwitcherController> provider2) {
        this.contextProvider = provider;
        this.userSwitcherControllerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public NotificationSensitiveController get() {
        return provideInstance(this.contextProvider, this.userSwitcherControllerProvider);
    }

    public static NotificationSensitiveController provideInstance(Provider<Context> provider, Provider<UserSwitcherController> provider2) {
        return new NotificationSensitiveController(provider.get(), provider2.get());
    }

    public static NotificationSensitiveController_Factory create(Provider<Context> provider, Provider<UserSwitcherController> provider2) {
        return new NotificationSensitiveController_Factory(provider, provider2);
    }
}
