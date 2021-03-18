package com.android.systemui.statusbar.dagger;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Handler;
import com.android.systemui.statusbar.NotificationListener;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class StatusBarDependenciesModule_ProvideNotificationListenerFactory implements Factory<NotificationListener> {
    private final Provider<Context> contextProvider;
    private final Provider<Handler> mainHandlerProvider;
    private final Provider<NotificationManager> notificationManagerProvider;

    public StatusBarDependenciesModule_ProvideNotificationListenerFactory(Provider<Context> provider, Provider<NotificationManager> provider2, Provider<Handler> provider3) {
        this.contextProvider = provider;
        this.notificationManagerProvider = provider2;
        this.mainHandlerProvider = provider3;
    }

    @Override // javax.inject.Provider
    public NotificationListener get() {
        return provideInstance(this.contextProvider, this.notificationManagerProvider, this.mainHandlerProvider);
    }

    public static NotificationListener provideInstance(Provider<Context> provider, Provider<NotificationManager> provider2, Provider<Handler> provider3) {
        return proxyProvideNotificationListener(provider.get(), provider2.get(), provider3.get());
    }

    public static StatusBarDependenciesModule_ProvideNotificationListenerFactory create(Provider<Context> provider, Provider<NotificationManager> provider2, Provider<Handler> provider3) {
        return new StatusBarDependenciesModule_ProvideNotificationListenerFactory(provider, provider2, provider3);
    }

    public static NotificationListener proxyProvideNotificationListener(Context context, NotificationManager notificationManager, Handler handler) {
        NotificationListener provideNotificationListener = StatusBarDependenciesModule.provideNotificationListener(context, notificationManager, handler);
        Preconditions.checkNotNull(provideNotificationListener, "Cannot return null from a non-@Nullable @Provides method");
        return provideNotificationListener;
    }
}
