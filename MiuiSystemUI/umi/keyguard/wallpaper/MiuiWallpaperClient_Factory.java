package com.android.keyguard.wallpaper;

import android.content.Context;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiWallpaperClient_Factory implements Factory<MiuiWallpaperClient> {
    private final Provider<Context> mContextProvider;
    private final Provider<WakefulnessLifecycle> wakefulnessLifecycleProvider;

    public MiuiWallpaperClient_Factory(Provider<Context> provider, Provider<WakefulnessLifecycle> provider2) {
        this.mContextProvider = provider;
        this.wakefulnessLifecycleProvider = provider2;
    }

    @Override // javax.inject.Provider
    public MiuiWallpaperClient get() {
        return provideInstance(this.mContextProvider, this.wakefulnessLifecycleProvider);
    }

    public static MiuiWallpaperClient provideInstance(Provider<Context> provider, Provider<WakefulnessLifecycle> provider2) {
        return new MiuiWallpaperClient(provider.get(), provider2.get());
    }

    public static MiuiWallpaperClient_Factory create(Provider<Context> provider, Provider<WakefulnessLifecycle> provider2) {
        return new MiuiWallpaperClient_Factory(provider, provider2);
    }
}
