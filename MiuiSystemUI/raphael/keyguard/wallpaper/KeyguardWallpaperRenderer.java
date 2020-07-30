package com.android.keyguard.wallpaper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Pair;
import com.android.systemui.glwallpaper.GLWallpaperRenderer;
import com.android.systemui.glwallpaper.ImageWallpaperRenderer;
import com.android.systemui.plugins.R;
import java.io.File;

public class KeyguardWallpaperRenderer extends ImageWallpaperRenderer {
    /* access modifiers changed from: protected */
    public boolean enableScissorMode() {
        return false;
    }

    /* access modifiers changed from: protected */
    public int getFragmentResId() {
        return R.raw.image_wallpaper_fragment_shader;
    }

    /* access modifiers changed from: protected */
    public int getVertexResId() {
        return R.raw.image_wallpaper_vertex_shader;
    }

    public KeyguardWallpaperRenderer(Context context, GLWallpaperRenderer.SurfaceProxy surfaceProxy) {
        super(context, surfaceProxy);
    }

    /* access modifiers changed from: protected */
    public Bitmap getBitmap() {
        Pair<File, Drawable> lockWallpaper = KeyguardWallpaperUtils.getLockWallpaper(this.mContext);
        if (lockWallpaper == null) {
            return null;
        }
        Object obj = lockWallpaper.second;
        if (obj instanceof BitmapDrawable) {
            return ((BitmapDrawable) obj).getBitmap();
        }
        return null;
    }
}
