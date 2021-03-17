package com.android.systemui;

import android.os.Handler;
import com.android.systemui.appops.AppOpsController;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ForegroundServiceController_Factory implements Factory<ForegroundServiceController> {
    private final Provider<AppOpsController> appOpsControllerProvider;
    private final Provider<NotificationEntryManager> entryManagerProvider;
    private final Provider<Handler> mainHandlerProvider;

    public ForegroundServiceController_Factory(Provider<NotificationEntryManager> provider, Provider<AppOpsController> provider2, Provider<Handler> provider3) {
        this.entryManagerProvider = provider;
        this.appOpsControllerProvider = provider2;
        this.mainHandlerProvider = provider3;
    }

    @Override // javax.inject.Provider
    public ForegroundServiceController get() {
        return provideInstance(this.entryManagerProvider, this.appOpsControllerProvider, this.mainHandlerProvider);
    }

    public static ForegroundServiceController provideInstance(Provider<NotificationEntryManager> provider, Provider<AppOpsController> provider2, Provider<Handler> provider3) {
        return new ForegroundServiceController(provider.get(), provider2.get(), provider3.get());
    }

    public static ForegroundServiceController_Factory create(Provider<NotificationEntryManager> provider, Provider<AppOpsController> provider2, Provider<Handler> provider3) {
        return new ForegroundServiceController_Factory(provider, provider2, provider3);
    }
}
