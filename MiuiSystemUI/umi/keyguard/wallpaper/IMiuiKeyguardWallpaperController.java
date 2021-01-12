package com.android.keyguard.wallpaper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: IMiuiKeyguardWallpaperController.kt */
public interface IMiuiKeyguardWallpaperController {

    /* compiled from: IMiuiKeyguardWallpaperController.kt */
    public interface IWallpaperChangeCallback {
        void onWallpaperChange(boolean z);
    }

    @Nullable
    String getCurrentWallpaperString();

    int getWallpaperBlurColor();

    boolean isSuperWallpaper();

    boolean isWallpaperColorLight();

    void registerWallpaperChangeCallback(@NotNull IWallpaperChangeCallback iWallpaperChangeCallback);

    void unregisterWallpaperChangeCallback(@NotNull IWallpaperChangeCallback iWallpaperChangeCallback);
}
