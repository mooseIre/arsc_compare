package com.android.systemui.statusbar.notification;

import android.content.Context;
import android.os.Handler;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ConversationNotificationManager_Factory implements Factory<ConversationNotificationManager> {
    private final Provider<Context> contextProvider;
    private final Provider<Handler> mainHandlerProvider;
    private final Provider<NotificationEntryManager> notificationEntryManagerProvider;
    private final Provider<NotificationGroupManager> notificationGroupManagerProvider;

    public ConversationNotificationManager_Factory(Provider<NotificationEntryManager> provider, Provider<NotificationGroupManager> provider2, Provider<Context> provider3, Provider<Handler> provider4) {
        this.notificationEntryManagerProvider = provider;
        this.notificationGroupManagerProvider = provider2;
        this.contextProvider = provider3;
        this.mainHandlerProvider = provider4;
    }

    @Override // javax.inject.Provider
    public ConversationNotificationManager get() {
        return provideInstance(this.notificationEntryManagerProvider, this.notificationGroupManagerProvider, this.contextProvider, this.mainHandlerProvider);
    }

    public static ConversationNotificationManager provideInstance(Provider<NotificationEntryManager> provider, Provider<NotificationGroupManager> provider2, Provider<Context> provider3, Provider<Handler> provider4) {
        return new ConversationNotificationManager(provider.get(), provider2.get(), provider3.get(), provider4.get());
    }

    public static ConversationNotificationManager_Factory create(Provider<NotificationEntryManager> provider, Provider<NotificationGroupManager> provider2, Provider<Context> provider3, Provider<Handler> provider4) {
        return new ConversationNotificationManager_Factory(provider, provider2, provider3, provider4);
    }
}
