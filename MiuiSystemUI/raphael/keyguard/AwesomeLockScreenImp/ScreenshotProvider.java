package com.android.keyguard.AwesomeLockScreenImp;

import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.SurfaceControl;
import com.android.keyguard.wallpaper.KeyguardWallpaperUtils;
import miui.maml.ScreenElementRoot;
import miui.maml.elements.BitmapProvider;

public class ScreenshotProvider extends BitmapProvider {
    public ScreenshotProvider(ScreenElementRoot screenElementRoot) {
        super(screenElementRoot);
    }

    public void reset() {
        ScreenshotProvider.super.reset();
        if (Boolean.parseBoolean(this.mRoot.getRawAttr("__is_secure"))) {
            Drawable lockWallpaperPreview = KeyguardWallpaperUtils.getLockWallpaperPreview(this.mRoot.getContext().mContext);
            if (lockWallpaperPreview instanceof BitmapDrawable) {
                this.mVersionedBitmap.setBitmap(((BitmapDrawable) lockWallpaperPreview).getBitmap());
                return;
            }
            return;
        }
        this.mVersionedBitmap.setBitmap(SurfaceControl.screenshot(new Rect(), this.mRoot.getScreenWidth(), this.mRoot.getScreenHeight(), false, 0));
    }
}
