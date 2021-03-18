package com.android.systemui.media;

import com.android.systemui.media.MediaHost;
import dagger.internal.Factory;

public final class MediaHost_MediaHostStateHolder_Factory implements Factory<MediaHost.MediaHostStateHolder> {
    private static final MediaHost_MediaHostStateHolder_Factory INSTANCE = new MediaHost_MediaHostStateHolder_Factory();

    @Override // javax.inject.Provider
    public MediaHost.MediaHostStateHolder get() {
        return provideInstance();
    }

    public static MediaHost.MediaHostStateHolder provideInstance() {
        return new MediaHost.MediaHostStateHolder();
    }

    public static MediaHost_MediaHostStateHolder_Factory create() {
        return INSTANCE;
    }
}
