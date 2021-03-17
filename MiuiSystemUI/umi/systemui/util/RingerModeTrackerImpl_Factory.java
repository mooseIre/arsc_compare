package com.android.systemui.util;

import android.media.AudioManager;
import com.android.systemui.broadcast.BroadcastDispatcher;
import dagger.internal.Factory;
import java.util.concurrent.Executor;
import javax.inject.Provider;

public final class RingerModeTrackerImpl_Factory implements Factory<RingerModeTrackerImpl> {
    private final Provider<AudioManager> audioManagerProvider;
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<Executor> executorProvider;

    public RingerModeTrackerImpl_Factory(Provider<AudioManager> provider, Provider<BroadcastDispatcher> provider2, Provider<Executor> provider3) {
        this.audioManagerProvider = provider;
        this.broadcastDispatcherProvider = provider2;
        this.executorProvider = provider3;
    }

    @Override // javax.inject.Provider
    public RingerModeTrackerImpl get() {
        return provideInstance(this.audioManagerProvider, this.broadcastDispatcherProvider, this.executorProvider);
    }

    public static RingerModeTrackerImpl provideInstance(Provider<AudioManager> provider, Provider<BroadcastDispatcher> provider2, Provider<Executor> provider3) {
        return new RingerModeTrackerImpl(provider.get(), provider2.get(), provider3.get());
    }

    public static RingerModeTrackerImpl_Factory create(Provider<AudioManager> provider, Provider<BroadcastDispatcher> provider2, Provider<Executor> provider3) {
        return new RingerModeTrackerImpl_Factory(provider, provider2, provider3);
    }
}
