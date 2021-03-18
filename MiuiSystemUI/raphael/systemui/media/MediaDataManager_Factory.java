package com.android.systemui.media;

import android.content.Context;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.dump.DumpManager;
import dagger.internal.Factory;
import java.util.concurrent.Executor;
import javax.inject.Provider;

public final class MediaDataManager_Factory implements Factory<MediaDataManager> {
    private final Provider<Executor> backgroundExecutorProvider;
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<Context> contextProvider;
    private final Provider<DumpManager> dumpManagerProvider;
    private final Provider<Executor> foregroundExecutorProvider;
    private final Provider<MediaControllerFactory> mediaControllerFactoryProvider;
    private final Provider<MediaResumeListener> mediaResumeListenerProvider;
    private final Provider<MediaTimeoutListener> mediaTimeoutListenerProvider;

    public MediaDataManager_Factory(Provider<Context> provider, Provider<Executor> provider2, Provider<Executor> provider3, Provider<MediaControllerFactory> provider4, Provider<DumpManager> provider5, Provider<BroadcastDispatcher> provider6, Provider<MediaTimeoutListener> provider7, Provider<MediaResumeListener> provider8) {
        this.contextProvider = provider;
        this.backgroundExecutorProvider = provider2;
        this.foregroundExecutorProvider = provider3;
        this.mediaControllerFactoryProvider = provider4;
        this.dumpManagerProvider = provider5;
        this.broadcastDispatcherProvider = provider6;
        this.mediaTimeoutListenerProvider = provider7;
        this.mediaResumeListenerProvider = provider8;
    }

    @Override // javax.inject.Provider
    public MediaDataManager get() {
        return provideInstance(this.contextProvider, this.backgroundExecutorProvider, this.foregroundExecutorProvider, this.mediaControllerFactoryProvider, this.dumpManagerProvider, this.broadcastDispatcherProvider, this.mediaTimeoutListenerProvider, this.mediaResumeListenerProvider);
    }

    public static MediaDataManager provideInstance(Provider<Context> provider, Provider<Executor> provider2, Provider<Executor> provider3, Provider<MediaControllerFactory> provider4, Provider<DumpManager> provider5, Provider<BroadcastDispatcher> provider6, Provider<MediaTimeoutListener> provider7, Provider<MediaResumeListener> provider8) {
        return new MediaDataManager(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get());
    }

    public static MediaDataManager_Factory create(Provider<Context> provider, Provider<Executor> provider2, Provider<Executor> provider3, Provider<MediaControllerFactory> provider4, Provider<DumpManager> provider5, Provider<BroadcastDispatcher> provider6, Provider<MediaTimeoutListener> provider7, Provider<MediaResumeListener> provider8) {
        return new MediaDataManager_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8);
    }
}
