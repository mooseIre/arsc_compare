package com.android.keyguard.AwesomeLockScreenImp;

import android.content.Context;
import java.io.File;
import miui.maml.ResourceManager;
import miui.maml.ScreenContext;
import miui.maml.ScreenElementRoot;
import miui.maml.util.ZipResourceLoader;

public class AccessibleElementRoot extends ScreenElementRoot {
    private boolean mInited = false;

    public AccessibleElementRoot(Context context, LockScreenRoot lockScreenRoot) {
        super(new ScreenContext(context, new ResourceManager(new ZipResourceLoader("/system/media/theme/default/virtuallockscreen"))));
        if (load()) {
            init();
            setOnExternCommandListener(lockScreenRoot);
            this.mInited = true;
        }
    }

    public boolean isInited() {
        return this.mInited;
    }

    public static boolean isFileExists() {
        return new File("/system/media/theme/default/virtuallockscreen").exists();
    }
}
