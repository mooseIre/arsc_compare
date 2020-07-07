package com.android.systemui.statusbar.phone;

import android.app.IWallpaperManagerCallback;

public class LockscreenWallpaper extends IWallpaperManagerCallback.Stub implements Runnable {
    public abstract void setCurrentUser(int i);
}
