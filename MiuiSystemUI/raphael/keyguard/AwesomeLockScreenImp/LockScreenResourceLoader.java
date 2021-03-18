package com.android.keyguard.AwesomeLockScreenImp;

import java.io.InputStream;
import miui.content.res.ThemeResources;
import miui.maml.ResourceLoader;

public class LockScreenResourceLoader extends ResourceLoader {
    public boolean resourceExists(String str) {
        return ThemeResources.getSystem().containsAwesomeLockscreenEntry(str);
    }

    public InputStream getInputStream(String str, long[] jArr) {
        return ThemeResources.getSystem().getAwesomeLockscreenFileStream(str, jArr);
    }
}
