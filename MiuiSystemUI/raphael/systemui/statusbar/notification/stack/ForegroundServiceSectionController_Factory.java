package com.android.systemui.statusbar.notification.stack;

import com.android.systemui.statusbar.notification.ForegroundServiceDismissalFeatureController;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ForegroundServiceSectionController_Factory implements Factory<ForegroundServiceSectionController> {
    private final Provider<NotificationEntryManager> entryManagerProvider;
    private final Provider<ForegroundServiceDismissalFeatureController> featureControllerProvider;

    public ForegroundServiceSectionController_Factory(Provider<NotificationEntryManager> provider, Provider<ForegroundServiceDismissalFeatureController> provider2) {
        this.entryManagerProvider = provider;
        this.featureControllerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public ForegroundServiceSectionController get() {
        return provideInstance(this.entryManagerProvider, this.featureControllerProvider);
    }

    public static ForegroundServiceSectionController provideInstance(Provider<NotificationEntryManager> provider, Provider<ForegroundServiceDismissalFeatureController> provider2) {
        return new ForegroundServiceSectionController(provider.get(), provider2.get());
    }

    public static ForegroundServiceSectionController_Factory create(Provider<NotificationEntryManager> provider, Provider<ForegroundServiceDismissalFeatureController> provider2) {
        return new ForegroundServiceSectionController_Factory(provider, provider2);
    }
}
