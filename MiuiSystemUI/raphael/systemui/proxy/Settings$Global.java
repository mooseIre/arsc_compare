package com.android.systemui.proxy;

import android.content.ContentResolver;
import android.provider.Settings;

public final class Settings$Global {
    public static int getInt(ContentResolver contentResolver, String str, int i) {
        return Settings.Global.getInt(contentResolver, str, i);
    }
}
