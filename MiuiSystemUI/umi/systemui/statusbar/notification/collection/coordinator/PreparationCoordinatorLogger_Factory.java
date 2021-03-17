package com.android.systemui.statusbar.notification.collection.coordinator;

import com.android.systemui.log.LogBuffer;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class PreparationCoordinatorLogger_Factory implements Factory<PreparationCoordinatorLogger> {
    private final Provider<LogBuffer> bufferProvider;

    public PreparationCoordinatorLogger_Factory(Provider<LogBuffer> provider) {
        this.bufferProvider = provider;
    }

    @Override // javax.inject.Provider
    public PreparationCoordinatorLogger get() {
        return provideInstance(this.bufferProvider);
    }

    public static PreparationCoordinatorLogger provideInstance(Provider<LogBuffer> provider) {
        return new PreparationCoordinatorLogger(provider.get());
    }

    public static PreparationCoordinatorLogger_Factory create(Provider<LogBuffer> provider) {
        return new PreparationCoordinatorLogger_Factory(provider);
    }
}
