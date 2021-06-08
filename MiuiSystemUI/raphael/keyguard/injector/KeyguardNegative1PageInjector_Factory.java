package com.android.keyguard.injector;

import android.content.Context;
import com.android.keyguard.wallpaper.IMiuiKeyguardWallpaperController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class KeyguardNegative1PageInjector_Factory implements Factory<KeyguardNegative1PageInjector> {
    private final Provider<Context> mContextProvider;
    private final Provider<IMiuiKeyguardWallpaperController> mWallpaperControllerProvider;

    public KeyguardNegative1PageInjector_Factory(Provider<Context> provider, Provider<IMiuiKeyguardWallpaperController> provider2) {
        this.mContextProvider = provider;
        this.mWallpaperControllerProvider = provider2;
    }

    @Override // javax.inject.Provider
    public KeyguardNegative1PageInjector get() {
        return provideInstance(this.mContextProvider, this.mWallpaperControllerProvider);
    }

    public static KeyguardNegative1PageInjector provideInstance(Provider<Context> provider, Provider<IMiuiKeyguardWallpaperController> provider2) {
        return new KeyguardNegative1PageInjector(provider.get(), provider2.get());
    }

    public static KeyguardNegative1PageInjector_Factory create(Provider<Context> provider, Provider<IMiuiKeyguardWallpaperController> provider2) {
        return new KeyguardNegative1PageInjector_Factory(provider, provider2);
    }
}
