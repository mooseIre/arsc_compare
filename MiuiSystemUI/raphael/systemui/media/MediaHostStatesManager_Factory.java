package com.android.systemui.media;

import dagger.internal.Factory;

public final class MediaHostStatesManager_Factory implements Factory<MediaHostStatesManager> {
    private static final MediaHostStatesManager_Factory INSTANCE = new MediaHostStatesManager_Factory();

    @Override // javax.inject.Provider
    public MediaHostStatesManager get() {
        return provideInstance();
    }

    public static MediaHostStatesManager provideInstance() {
        return new MediaHostStatesManager();
    }

    public static MediaHostStatesManager_Factory create() {
        return INSTANCE;
    }
}
