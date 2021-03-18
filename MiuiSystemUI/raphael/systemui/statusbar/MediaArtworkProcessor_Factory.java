package com.android.systemui.statusbar;

import dagger.internal.Factory;

public final class MediaArtworkProcessor_Factory implements Factory<MediaArtworkProcessor> {
    private static final MediaArtworkProcessor_Factory INSTANCE = new MediaArtworkProcessor_Factory();

    @Override // javax.inject.Provider
    public MediaArtworkProcessor get() {
        return provideInstance();
    }

    public static MediaArtworkProcessor provideInstance() {
        return new MediaArtworkProcessor();
    }

    public static MediaArtworkProcessor_Factory create() {
        return INSTANCE;
    }
}
