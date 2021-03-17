package com.android.systemui.broadcast.logging;

import com.android.systemui.log.LogBuffer;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class BroadcastDispatcherLogger_Factory implements Factory<BroadcastDispatcherLogger> {
    private final Provider<LogBuffer> bufferProvider;

    public BroadcastDispatcherLogger_Factory(Provider<LogBuffer> provider) {
        this.bufferProvider = provider;
    }

    @Override // javax.inject.Provider
    public BroadcastDispatcherLogger get() {
        return provideInstance(this.bufferProvider);
    }

    public static BroadcastDispatcherLogger provideInstance(Provider<LogBuffer> provider) {
        return new BroadcastDispatcherLogger(provider.get());
    }

    public static BroadcastDispatcherLogger_Factory create(Provider<LogBuffer> provider) {
        return new BroadcastDispatcherLogger_Factory(provider);
    }
}
