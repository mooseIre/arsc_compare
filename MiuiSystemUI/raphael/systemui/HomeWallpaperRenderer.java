package com.android.systemui;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import com.android.keyguard.MiuiFastUnlockController;
import com.android.systemui.glwallpaper.GLWallpaperRenderer;
import com.android.systemui.glwallpaper.ImageWallpaperRenderer;
import com.android.systemui.plugins.R;

public class HomeWallpaperRenderer extends ImageWallpaperRenderer {
    /* access modifiers changed from: protected */
    public int getFragmentResId() {
        return R.raw.home_wallpaper_fragment_shader;
    }

    /* access modifiers changed from: protected */
    public int getVertexResId() {
        return R.raw.image_wallpaper_vertex_shader;
    }

    public HomeWallpaperRenderer(Context context, GLWallpaperRenderer.SurfaceProxy surfaceProxy) {
        super(context, surfaceProxy);
    }

    /* access modifiers changed from: protected */
    public Bitmap getBitmap() {
        WallpaperManager wallpaperManager = this.mWallpaperManager;
        if (wallpaperManager != null) {
            return wallpaperManager.getBitmap();
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public boolean enableScissorMode() {
        return !((MiuiFastUnlockController) Dependency.get(MiuiFastUnlockController.class)).isFastUnlock();
    }
}
