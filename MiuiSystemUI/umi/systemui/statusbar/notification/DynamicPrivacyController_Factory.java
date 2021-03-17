package com.android.systemui.statusbar.notification;

import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class DynamicPrivacyController_Factory implements Factory<DynamicPrivacyController> {
    private final Provider<KeyguardStateController> keyguardStateControllerProvider;
    private final Provider<NotificationLockscreenUserManager> notificationLockscreenUserManagerProvider;
    private final Provider<StatusBarStateController> stateControllerProvider;

    public DynamicPrivacyController_Factory(Provider<NotificationLockscreenUserManager> provider, Provider<KeyguardStateController> provider2, Provider<StatusBarStateController> provider3) {
        this.notificationLockscreenUserManagerProvider = provider;
        this.keyguardStateControllerProvider = provider2;
        this.stateControllerProvider = provider3;
    }

    @Override // javax.inject.Provider
    public DynamicPrivacyController get() {
        return provideInstance(this.notificationLockscreenUserManagerProvider, this.keyguardStateControllerProvider, this.stateControllerProvider);
    }

    public static DynamicPrivacyController provideInstance(Provider<NotificationLockscreenUserManager> provider, Provider<KeyguardStateController> provider2, Provider<StatusBarStateController> provider3) {
        return new DynamicPrivacyController(provider.get(), provider2.get(), provider3.get());
    }

    public static DynamicPrivacyController_Factory create(Provider<NotificationLockscreenUserManager> provider, Provider<KeyguardStateController> provider2, Provider<StatusBarStateController> provider3) {
        return new DynamicPrivacyController_Factory(provider, provider2, provider3);
    }
}
