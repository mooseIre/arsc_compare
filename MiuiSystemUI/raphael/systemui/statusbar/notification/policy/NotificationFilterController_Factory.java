package com.android.systemui.statusbar.notification.policy;

import android.content.Context;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.statusbar.NotificationListener;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationSettingsManager;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NotificationFilterController_Factory implements Factory<NotificationFilterController> {
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<Context> contextProvider;
    private final Provider<NotificationEntryManager> entryManagerProvider;
    private final Provider<NotificationListener> notificationListenerProvider;
    private final Provider<NotificationSettingsManager> settingsManagerProvider;

    public NotificationFilterController_Factory(Provider<Context> provider, Provider<NotificationListener> provider2, Provider<NotificationEntryManager> provider3, Provider<NotificationSettingsManager> provider4, Provider<BroadcastDispatcher> provider5) {
        this.contextProvider = provider;
        this.notificationListenerProvider = provider2;
        this.entryManagerProvider = provider3;
        this.settingsManagerProvider = provider4;
        this.broadcastDispatcherProvider = provider5;
    }

    @Override // javax.inject.Provider
    public NotificationFilterController get() {
        return provideInstance(this.contextProvider, this.notificationListenerProvider, this.entryManagerProvider, this.settingsManagerProvider, this.broadcastDispatcherProvider);
    }

    public static NotificationFilterController provideInstance(Provider<Context> provider, Provider<NotificationListener> provider2, Provider<NotificationEntryManager> provider3, Provider<NotificationSettingsManager> provider4, Provider<BroadcastDispatcher> provider5) {
        return new NotificationFilterController(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get());
    }

    public static NotificationFilterController_Factory create(Provider<Context> provider, Provider<NotificationListener> provider2, Provider<NotificationEntryManager> provider3, Provider<NotificationSettingsManager> provider4, Provider<BroadcastDispatcher> provider5) {
        return new NotificationFilterController_Factory(provider, provider2, provider3, provider4, provider5);
    }
}
