package com.android.systemui.statusbar.notification.stack;

import com.android.systemui.log.LogBuffer;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NotificationSectionsLogger_Factory implements Factory<NotificationSectionsLogger> {
    private final Provider<LogBuffer> logBufferProvider;

    public NotificationSectionsLogger_Factory(Provider<LogBuffer> provider) {
        this.logBufferProvider = provider;
    }

    @Override // javax.inject.Provider
    public NotificationSectionsLogger get() {
        return provideInstance(this.logBufferProvider);
    }

    public static NotificationSectionsLogger provideInstance(Provider<LogBuffer> provider) {
        return new NotificationSectionsLogger(provider.get());
    }

    public static NotificationSectionsLogger_Factory create(Provider<LogBuffer> provider) {
        return new NotificationSectionsLogger_Factory(provider);
    }
}
