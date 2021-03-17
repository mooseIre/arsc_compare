package com.android.systemui.statusbar.notification.stack;

import com.android.systemui.statusbar.notification.MiuiNotificationSectionsFeatureManager;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NotificationRoundnessManager_Factory implements Factory<NotificationRoundnessManager> {
    private final Provider<KeyguardBypassController> keyguardBypassControllerProvider;
    private final Provider<MiuiNotificationSectionsFeatureManager> sectionsFeatureManagerProvider;

    public NotificationRoundnessManager_Factory(Provider<KeyguardBypassController> provider, Provider<MiuiNotificationSectionsFeatureManager> provider2) {
        this.keyguardBypassControllerProvider = provider;
        this.sectionsFeatureManagerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public NotificationRoundnessManager get() {
        return provideInstance(this.keyguardBypassControllerProvider, this.sectionsFeatureManagerProvider);
    }

    public static NotificationRoundnessManager provideInstance(Provider<KeyguardBypassController> provider, Provider<MiuiNotificationSectionsFeatureManager> provider2) {
        return new NotificationRoundnessManager(provider.get(), provider2.get());
    }

    public static NotificationRoundnessManager_Factory create(Provider<KeyguardBypassController> provider, Provider<MiuiNotificationSectionsFeatureManager> provider2) {
        return new NotificationRoundnessManager_Factory(provider, provider2);
    }
}
