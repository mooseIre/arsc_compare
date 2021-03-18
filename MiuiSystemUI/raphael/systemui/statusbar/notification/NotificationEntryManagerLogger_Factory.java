package com.android.systemui.statusbar.notification;

import com.android.systemui.log.LogBuffer;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NotificationEntryManagerLogger_Factory implements Factory<NotificationEntryManagerLogger> {
    private final Provider<LogBuffer> bufferProvider;

    public NotificationEntryManagerLogger_Factory(Provider<LogBuffer> provider) {
        this.bufferProvider = provider;
    }

    @Override // javax.inject.Provider
    public NotificationEntryManagerLogger get() {
        return provideInstance(this.bufferProvider);
    }

    public static NotificationEntryManagerLogger provideInstance(Provider<LogBuffer> provider) {
        return new NotificationEntryManagerLogger(provider.get());
    }

    public static NotificationEntryManagerLogger_Factory create(Provider<LogBuffer> provider) {
        return new NotificationEntryManagerLogger_Factory(provider);
    }
}
