package com.android.systemui.statusbar.notification.collection.notifcollection;

import com.android.systemui.log.LogBuffer;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NotifCollectionLogger_Factory implements Factory<NotifCollectionLogger> {
    private final Provider<LogBuffer> bufferProvider;

    public NotifCollectionLogger_Factory(Provider<LogBuffer> provider) {
        this.bufferProvider = provider;
    }

    @Override // javax.inject.Provider
    public NotifCollectionLogger get() {
        return provideInstance(this.bufferProvider);
    }

    public static NotifCollectionLogger provideInstance(Provider<LogBuffer> provider) {
        return new NotifCollectionLogger(provider.get());
    }

    public static NotifCollectionLogger_Factory create(Provider<LogBuffer> provider) {
        return new NotifCollectionLogger_Factory(provider);
    }
}
