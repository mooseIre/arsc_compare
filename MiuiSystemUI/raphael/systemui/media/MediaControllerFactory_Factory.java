package com.android.systemui.media;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MediaControllerFactory_Factory implements Factory<MediaControllerFactory> {
    private final Provider<Context> contextProvider;

    public MediaControllerFactory_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public MediaControllerFactory get() {
        return provideInstance(this.contextProvider);
    }

    public static MediaControllerFactory provideInstance(Provider<Context> provider) {
        return new MediaControllerFactory(provider.get());
    }

    public static MediaControllerFactory_Factory create(Provider<Context> provider) {
        return new MediaControllerFactory_Factory(provider);
    }
}
