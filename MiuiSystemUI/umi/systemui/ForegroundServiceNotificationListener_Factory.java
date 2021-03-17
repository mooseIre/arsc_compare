package com.android.systemui;

import android.content.Context;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.util.time.SystemClock;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ForegroundServiceNotificationListener_Factory implements Factory<ForegroundServiceNotificationListener> {
    private final Provider<Context> contextProvider;
    private final Provider<ForegroundServiceLifetimeExtender> fgsLifetimeExtenderProvider;
    private final Provider<ForegroundServiceController> foregroundServiceControllerProvider;
    private final Provider<NotifPipeline> notifPipelineProvider;
    private final Provider<NotificationEntryManager> notificationEntryManagerProvider;
    private final Provider<SystemClock> systemClockProvider;

    public ForegroundServiceNotificationListener_Factory(Provider<Context> provider, Provider<ForegroundServiceController> provider2, Provider<NotificationEntryManager> provider3, Provider<NotifPipeline> provider4, Provider<ForegroundServiceLifetimeExtender> provider5, Provider<SystemClock> provider6) {
        this.contextProvider = provider;
        this.foregroundServiceControllerProvider = provider2;
        this.notificationEntryManagerProvider = provider3;
        this.notifPipelineProvider = provider4;
        this.fgsLifetimeExtenderProvider = provider5;
        this.systemClockProvider = provider6;
    }

    @Override // javax.inject.Provider
    public ForegroundServiceNotificationListener get() {
        return provideInstance(this.contextProvider, this.foregroundServiceControllerProvider, this.notificationEntryManagerProvider, this.notifPipelineProvider, this.fgsLifetimeExtenderProvider, this.systemClockProvider);
    }

    public static ForegroundServiceNotificationListener provideInstance(Provider<Context> provider, Provider<ForegroundServiceController> provider2, Provider<NotificationEntryManager> provider3, Provider<NotifPipeline> provider4, Provider<ForegroundServiceLifetimeExtender> provider5, Provider<SystemClock> provider6) {
        return new ForegroundServiceNotificationListener(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get());
    }

    public static ForegroundServiceNotificationListener_Factory create(Provider<Context> provider, Provider<ForegroundServiceController> provider2, Provider<NotificationEntryManager> provider3, Provider<NotifPipeline> provider4, Provider<ForegroundServiceLifetimeExtender> provider5, Provider<SystemClock> provider6) {
        return new ForegroundServiceNotificationListener_Factory(provider, provider2, provider3, provider4, provider5, provider6);
    }
}
