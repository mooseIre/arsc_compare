package com.android.keyguard.AwesomeLockScreenImp;

import android.text.TextUtils;
import miui.maml.ObjectFactory;
import miui.maml.ScreenElementRoot;
import miui.maml.elements.BitmapProvider;

public class LockscreenBitmapProviderFactory extends ObjectFactory.BitmapProviderFactory {
    /* access modifiers changed from: protected */
    public BitmapProvider doCreate(ScreenElementRoot screenElementRoot, String str) {
        if (TextUtils.equals(str, "Screenshot")) {
            return new ScreenshotProvider(screenElementRoot);
        }
        return null;
    }
}
