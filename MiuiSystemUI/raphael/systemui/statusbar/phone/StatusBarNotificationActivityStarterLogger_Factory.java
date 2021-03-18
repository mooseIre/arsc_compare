package com.android.systemui.statusbar.phone;

import com.android.systemui.log.LogBuffer;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class StatusBarNotificationActivityStarterLogger_Factory implements Factory<StatusBarNotificationActivityStarterLogger> {
    private final Provider<LogBuffer> bufferProvider;

    public StatusBarNotificationActivityStarterLogger_Factory(Provider<LogBuffer> provider) {
        this.bufferProvider = provider;
    }

    @Override // javax.inject.Provider
    public StatusBarNotificationActivityStarterLogger get() {
        return provideInstance(this.bufferProvider);
    }

    public static StatusBarNotificationActivityStarterLogger provideInstance(Provider<LogBuffer> provider) {
        return new StatusBarNotificationActivityStarterLogger(provider.get());
    }

    public static StatusBarNotificationActivityStarterLogger_Factory create(Provider<LogBuffer> provider) {
        return new StatusBarNotificationActivityStarterLogger_Factory(provider);
    }
}
