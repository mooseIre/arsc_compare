package com.android.systemui.dagger;

import android.app.IWallpaperManager;
import dagger.internal.Factory;

public final class SystemServicesModule_ProvideIWallPaperManagerFactory implements Factory<IWallpaperManager> {
    private static final SystemServicesModule_ProvideIWallPaperManagerFactory INSTANCE = new SystemServicesModule_ProvideIWallPaperManagerFactory();

    @Override // javax.inject.Provider
    public IWallpaperManager get() {
        return provideInstance();
    }

    public static IWallpaperManager provideInstance() {
        return proxyProvideIWallPaperManager();
    }

    public static SystemServicesModule_ProvideIWallPaperManagerFactory create() {
        return INSTANCE;
    }

    public static IWallpaperManager proxyProvideIWallPaperManager() {
        return SystemServicesModule.provideIWallPaperManager();
    }
}
