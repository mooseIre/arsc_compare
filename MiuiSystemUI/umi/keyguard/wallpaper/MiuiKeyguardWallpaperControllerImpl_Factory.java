package com.android.keyguard.wallpaper;

import android.content.Context;
import com.android.systemui.broadcast.BroadcastDispatcher;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiKeyguardWallpaperControllerImpl_Factory implements Factory<MiuiKeyguardWallpaperControllerImpl> {
    private final Provider<BroadcastDispatcher> mBroadcastDispatcherProvider;
    private final Provider<Context> mContextProvider;

    public MiuiKeyguardWallpaperControllerImpl_Factory(Provider<Context> provider, Provider<BroadcastDispatcher> provider2) {
        this.mContextProvider = provider;
        this.mBroadcastDispatcherProvider = provider2;
    }

    @Override // javax.inject.Provider
    public MiuiKeyguardWallpaperControllerImpl get() {
        return provideInstance(this.mContextProvider, this.mBroadcastDispatcherProvider);
    }

    public static MiuiKeyguardWallpaperControllerImpl provideInstance(Provider<Context> provider, Provider<BroadcastDispatcher> provider2) {
        return new MiuiKeyguardWallpaperControllerImpl(provider.get(), provider2.get());
    }

    public static MiuiKeyguardWallpaperControllerImpl_Factory create(Provider<Context> provider, Provider<BroadcastDispatcher> provider2) {
        return new MiuiKeyguardWallpaperControllerImpl_Factory(provider, provider2);
    }
}
