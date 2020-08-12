package com.android.systemui.glwallpaper;

public class ImageDarkenHelper {
    private boolean inDarkWallpaperMode;
    private boolean isDarken;

    public boolean isInDarkWallpaperMode() {
        return this.inDarkWallpaperMode;
    }

    public void setInDarkWallpaperMode(boolean z) {
        this.inDarkWallpaperMode = z;
    }

    public boolean isDarken() {
        return this.isDarken;
    }

    public void setDarken(boolean z) {
        this.isDarken = z;
    }
}
