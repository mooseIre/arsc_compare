package com.android.systemui.media;

import android.content.Context;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.tuner.TunerService;
import dagger.internal.Factory;
import java.util.concurrent.Executor;
import javax.inject.Provider;

public final class MediaResumeListener_Factory implements Factory<MediaResumeListener> {
    private final Provider<Executor> backgroundExecutorProvider;
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<Context> contextProvider;
    private final Provider<TunerService> tunerServiceProvider;

    public MediaResumeListener_Factory(Provider<Context> provider, Provider<BroadcastDispatcher> provider2, Provider<Executor> provider3, Provider<TunerService> provider4) {
        this.contextProvider = provider;
        this.broadcastDispatcherProvider = provider2;
        this.backgroundExecutorProvider = provider3;
        this.tunerServiceProvider = provider4;
    }

    @Override // javax.inject.Provider
    public MediaResumeListener get() {
        return provideInstance(this.contextProvider, this.broadcastDispatcherProvider, this.backgroundExecutorProvider, this.tunerServiceProvider);
    }

    public static MediaResumeListener provideInstance(Provider<Context> provider, Provider<BroadcastDispatcher> provider2, Provider<Executor> provider3, Provider<TunerService> provider4) {
        return new MediaResumeListener(provider.get(), provider2.get(), provider3.get(), provider4.get());
    }

    public static MediaResumeListener_Factory create(Provider<Context> provider, Provider<BroadcastDispatcher> provider2, Provider<Executor> provider3, Provider<TunerService> provider4) {
        return new MediaResumeListener_Factory(provider, provider2, provider3, provider4);
    }
}
