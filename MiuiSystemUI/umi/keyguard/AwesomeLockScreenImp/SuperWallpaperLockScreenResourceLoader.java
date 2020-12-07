package com.android.keyguard.AwesomeLockScreenImp;

import java.io.InputStream;
import miui.content.res.ThemeResources;
import miui.maml.ResourceLoader;

public class SuperWallpaperLockScreenResourceLoader extends ResourceLoader {
    public boolean resourceExists(String str) {
        return ThemeResources.getSystem().containsSuperWallpaperLockscreenEntry(str);
    }

    public InputStream getInputStream(String str, long[] jArr) {
        return ThemeResources.getSystem().getSuperWallpaperLockscreenFileStream(str, jArr);
    }
}
