package com.android.systemui.statusbar.notification.policy;

import android.content.Context;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class KeyguardNotificationController_Factory implements Factory<KeyguardNotificationController> {
    private final Provider<Context> contextProvider;
    private final Provider<NotificationEntryManager> entryManagerProvider;
    private final Provider<NotificationGroupManager> groupManagerProvider;
    private final Provider<KeyguardStateController> keyguardStateControllerProvider;

    public KeyguardNotificationController_Factory(Provider<Context> provider, Provider<NotificationEntryManager> provider2, Provider<NotificationGroupManager> provider3, Provider<KeyguardStateController> provider4) {
        this.contextProvider = provider;
        this.entryManagerProvider = provider2;
        this.groupManagerProvider = provider3;
        this.keyguardStateControllerProvider = provider4;
    }

    public KeyguardNotificationController get() {
        return provideInstance(this.contextProvider, this.entryManagerProvider, this.groupManagerProvider, this.keyguardStateControllerProvider);
    }

    public static KeyguardNotificationController provideInstance(Provider<Context> provider, Provider<NotificationEntryManager> provider2, Provider<NotificationGroupManager> provider3, Provider<KeyguardStateController> provider4) {
        return new KeyguardNotificationController(provider.get(), provider2.get(), provider3.get(), provider4.get());
    }

    public static KeyguardNotificationController_Factory create(Provider<Context> provider, Provider<NotificationEntryManager> provider2, Provider<NotificationGroupManager> provider3, Provider<KeyguardStateController> provider4) {
        return new KeyguardNotificationController_Factory(provider, provider2, provider3, provider4);
    }
}
