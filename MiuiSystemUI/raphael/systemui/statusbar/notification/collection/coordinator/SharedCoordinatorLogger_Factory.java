package com.android.systemui.statusbar.notification.collection.coordinator;

import com.android.systemui.log.LogBuffer;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class SharedCoordinatorLogger_Factory implements Factory<SharedCoordinatorLogger> {
    private final Provider<LogBuffer> bufferProvider;

    public SharedCoordinatorLogger_Factory(Provider<LogBuffer> provider) {
        this.bufferProvider = provider;
    }

    @Override // javax.inject.Provider
    public SharedCoordinatorLogger get() {
        return provideInstance(this.bufferProvider);
    }

    public static SharedCoordinatorLogger provideInstance(Provider<LogBuffer> provider) {
        return new SharedCoordinatorLogger(provider.get());
    }

    public static SharedCoordinatorLogger_Factory create(Provider<LogBuffer> provider) {
        return new SharedCoordinatorLogger_Factory(provider);
    }
}
