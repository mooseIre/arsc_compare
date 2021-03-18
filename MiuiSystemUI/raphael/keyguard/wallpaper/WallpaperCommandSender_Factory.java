package com.android.keyguard.wallpaper;

import dagger.internal.Factory;

public final class WallpaperCommandSender_Factory implements Factory<WallpaperCommandSender> {
    private static final WallpaperCommandSender_Factory INSTANCE = new WallpaperCommandSender_Factory();

    @Override // javax.inject.Provider
    public WallpaperCommandSender get() {
        return provideInstance();
    }

    public static WallpaperCommandSender provideInstance() {
        return new WallpaperCommandSender();
    }

    public static WallpaperCommandSender_Factory create() {
        return INSTANCE;
    }
}
