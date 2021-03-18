package com.android.systemui.statusbar.phone;

import android.view.WindowManager;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class LightsOutNotifController_Factory implements Factory<LightsOutNotifController> {
    private final Provider<CommandQueue> commandQueueProvider;
    private final Provider<NotificationEntryManager> entryManagerProvider;
    private final Provider<WindowManager> windowManagerProvider;

    public LightsOutNotifController_Factory(Provider<WindowManager> provider, Provider<NotificationEntryManager> provider2, Provider<CommandQueue> provider3) {
        this.windowManagerProvider = provider;
        this.entryManagerProvider = provider2;
        this.commandQueueProvider = provider3;
    }

    @Override // javax.inject.Provider
    public LightsOutNotifController get() {
        return provideInstance(this.windowManagerProvider, this.entryManagerProvider, this.commandQueueProvider);
    }

    public static LightsOutNotifController provideInstance(Provider<WindowManager> provider, Provider<NotificationEntryManager> provider2, Provider<CommandQueue> provider3) {
        return new LightsOutNotifController(provider.get(), provider2.get(), provider3.get());
    }

    public static LightsOutNotifController_Factory create(Provider<WindowManager> provider, Provider<NotificationEntryManager> provider2, Provider<CommandQueue> provider3) {
        return new LightsOutNotifController_Factory(provider, provider2, provider3);
    }
}
