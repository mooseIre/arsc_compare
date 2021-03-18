package com.android.systemui.shared.system;

import android.app.WallpaperManager;
import android.content.Context;
import android.os.IBinder;

public class WallpaperManagerCompat {
    private final WallpaperManager mWallpaperManager;

    public WallpaperManagerCompat(Context context) {
        this.mWallpaperManager = (WallpaperManager) context.getSystemService(WallpaperManager.class);
    }

    public void setWallpaperZoomOut(IBinder iBinder, float f) {
        this.mWallpaperManager.setWallpaperZoomOut(iBinder, f);
    }
}
