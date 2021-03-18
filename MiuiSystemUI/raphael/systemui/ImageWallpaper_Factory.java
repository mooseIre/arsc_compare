package com.android.systemui;

import dagger.internal.Factory;

public final class ImageWallpaper_Factory implements Factory<ImageWallpaper> {
    private static final ImageWallpaper_Factory INSTANCE = new ImageWallpaper_Factory();

    @Override // javax.inject.Provider
    public ImageWallpaper get() {
        return provideInstance();
    }

    public static ImageWallpaper provideInstance() {
        return new ImageWallpaper();
    }

    public static ImageWallpaper_Factory create() {
        return INSTANCE;
    }
}
