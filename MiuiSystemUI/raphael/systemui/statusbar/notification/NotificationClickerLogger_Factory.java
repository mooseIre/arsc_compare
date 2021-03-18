package com.android.systemui.statusbar.notification;

import com.android.systemui.log.LogBuffer;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NotificationClickerLogger_Factory implements Factory<NotificationClickerLogger> {
    private final Provider<LogBuffer> bufferProvider;

    public NotificationClickerLogger_Factory(Provider<LogBuffer> provider) {
        this.bufferProvider = provider;
    }

    @Override // javax.inject.Provider
    public NotificationClickerLogger get() {
        return provideInstance(this.bufferProvider);
    }

    public static NotificationClickerLogger provideInstance(Provider<LogBuffer> provider) {
        return new NotificationClickerLogger(provider.get());
    }

    public static NotificationClickerLogger_Factory create(Provider<LogBuffer> provider) {
        return new NotificationClickerLogger_Factory(provider);
    }
}
