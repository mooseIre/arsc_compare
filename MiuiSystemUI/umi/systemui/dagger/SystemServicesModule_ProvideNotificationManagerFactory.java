package com.android.systemui.dagger;

import android.app.NotificationManager;
import android.content.Context;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class SystemServicesModule_ProvideNotificationManagerFactory implements Factory<NotificationManager> {
    private final Provider<Context> contextProvider;

    public SystemServicesModule_ProvideNotificationManagerFactory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public NotificationManager get() {
        return provideInstance(this.contextProvider);
    }

    public static NotificationManager provideInstance(Provider<Context> provider) {
        return proxyProvideNotificationManager(provider.get());
    }

    public static SystemServicesModule_ProvideNotificationManagerFactory create(Provider<Context> provider) {
        return new SystemServicesModule_ProvideNotificationManagerFactory(provider);
    }

    public static NotificationManager proxyProvideNotificationManager(Context context) {
        NotificationManager provideNotificationManager = SystemServicesModule.provideNotificationManager(context);
        Preconditions.checkNotNull(provideNotificationManager, "Cannot return null from a non-@Nullable @Provides method");
        return provideNotificationManager;
    }
}
