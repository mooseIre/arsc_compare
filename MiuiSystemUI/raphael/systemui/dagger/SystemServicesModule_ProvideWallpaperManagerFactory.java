package com.android.systemui.dagger;

import android.app.WallpaperManager;
import android.content.Context;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class SystemServicesModule_ProvideWallpaperManagerFactory implements Factory<WallpaperManager> {
    private final Provider<Context> contextProvider;

    public SystemServicesModule_ProvideWallpaperManagerFactory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public WallpaperManager get() {
        return provideInstance(this.contextProvider);
    }

    public static WallpaperManager provideInstance(Provider<Context> provider) {
        return proxyProvideWallpaperManager(provider.get());
    }

    public static SystemServicesModule_ProvideWallpaperManagerFactory create(Provider<Context> provider) {
        return new SystemServicesModule_ProvideWallpaperManagerFactory(provider);
    }

    public static WallpaperManager proxyProvideWallpaperManager(Context context) {
        WallpaperManager provideWallpaperManager = SystemServicesModule.provideWallpaperManager(context);
        Preconditions.checkNotNull(provideWallpaperManager, "Cannot return null from a non-@Nullable @Provides method");
        return provideWallpaperManager;
    }
}
