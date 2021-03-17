package com.android.systemui.media;

import com.android.systemui.util.concurrency.DelayableExecutor;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MediaTimeoutListener_Factory implements Factory<MediaTimeoutListener> {
    private final Provider<DelayableExecutor> mainExecutorProvider;
    private final Provider<MediaControllerFactory> mediaControllerFactoryProvider;

    public MediaTimeoutListener_Factory(Provider<MediaControllerFactory> provider, Provider<DelayableExecutor> provider2) {
        this.mediaControllerFactoryProvider = provider;
        this.mainExecutorProvider = provider2;
    }

    @Override // javax.inject.Provider
    public MediaTimeoutListener get() {
        return provideInstance(this.mediaControllerFactoryProvider, this.mainExecutorProvider);
    }

    public static MediaTimeoutListener provideInstance(Provider<MediaControllerFactory> provider, Provider<DelayableExecutor> provider2) {
        return new MediaTimeoutListener(provider.get(), provider2.get());
    }

    public static MediaTimeoutListener_Factory create(Provider<MediaControllerFactory> provider, Provider<DelayableExecutor> provider2) {
        return new MediaTimeoutListener_Factory(provider, provider2);
    }
}
