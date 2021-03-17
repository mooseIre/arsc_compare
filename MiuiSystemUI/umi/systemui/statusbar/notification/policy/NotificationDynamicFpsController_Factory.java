package com.android.systemui.statusbar.notification.policy;

import android.content.Context;
import com.android.systemui.keyguard.ScreenLifecycle;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NotificationDynamicFpsController_Factory implements Factory<NotificationDynamicFpsController> {
    private final Provider<Context> contextProvider;
    private final Provider<HeadsUpManager> headsUpManagerProvider;
    private final Provider<NotificationEntryManager> notificationEntryManagerProvider;
    private final Provider<ScreenLifecycle> screenLifecycleProvider;
    private final Provider<StatusBar> statusBarProvider;
    private final Provider<StatusBarStateController> statusBarStateControllerProvider;

    public NotificationDynamicFpsController_Factory(Provider<Context> provider, Provider<NotificationEntryManager> provider2, Provider<HeadsUpManager> provider3, Provider<StatusBar> provider4, Provider<StatusBarStateController> provider5, Provider<ScreenLifecycle> provider6) {
        this.contextProvider = provider;
        this.notificationEntryManagerProvider = provider2;
        this.headsUpManagerProvider = provider3;
        this.statusBarProvider = provider4;
        this.statusBarStateControllerProvider = provider5;
        this.screenLifecycleProvider = provider6;
    }

    @Override // javax.inject.Provider
    public NotificationDynamicFpsController get() {
        return provideInstance(this.contextProvider, this.notificationEntryManagerProvider, this.headsUpManagerProvider, this.statusBarProvider, this.statusBarStateControllerProvider, this.screenLifecycleProvider);
    }

    public static NotificationDynamicFpsController provideInstance(Provider<Context> provider, Provider<NotificationEntryManager> provider2, Provider<HeadsUpManager> provider3, Provider<StatusBar> provider4, Provider<StatusBarStateController> provider5, Provider<ScreenLifecycle> provider6) {
        return new NotificationDynamicFpsController(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get());
    }

    public static NotificationDynamicFpsController_Factory create(Provider<Context> provider, Provider<NotificationEntryManager> provider2, Provider<HeadsUpManager> provider3, Provider<StatusBar> provider4, Provider<StatusBarStateController> provider5, Provider<ScreenLifecycle> provider6) {
        return new NotificationDynamicFpsController_Factory(provider, provider2, provider3, provider4, provider5, provider6);
    }
}
