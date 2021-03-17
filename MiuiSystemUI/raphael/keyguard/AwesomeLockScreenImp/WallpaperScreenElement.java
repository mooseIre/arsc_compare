package com.android.keyguard.AwesomeLockScreenImp;

import android.graphics.drawable.BitmapDrawable;
import com.android.keyguard.wallpaper.KeyguardWallpaperUtils;
import miui.maml.ScreenElementRoot;
import miui.maml.elements.ImageScreenElement;
import org.w3c.dom.Element;

public class WallpaperScreenElement extends ImageScreenElement {
    public WallpaperScreenElement(Element element, ScreenElementRoot screenElementRoot) {
        super(element, screenElementRoot);
    }

    public void init() {
        WallpaperScreenElement.super.init();
        BitmapDrawable bitmapDrawable = (BitmapDrawable) KeyguardWallpaperUtils.getLockWallpaperPreview(getContext().mContext);
        if (bitmapDrawable != null) {
            setBitmap(bitmapDrawable.getBitmap());
        }
    }
}
