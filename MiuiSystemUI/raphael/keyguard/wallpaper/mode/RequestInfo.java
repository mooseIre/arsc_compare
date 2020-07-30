package com.android.keyguard.wallpaper.mode;

import java.util.List;

public class RequestInfo {
    public WallpaperInfo currentWallpaperInfo;
    public int mode;
    public boolean needLast;
    public String packageName;
    public List<WallpaperInfo> wallpaperInfos;

    public String toString() {
        return "RequestInfo [mode=" + this.mode + ", currentWallpaperInfo=" + this.currentWallpaperInfo + ", needLast=" + this.needLast + ", wallpaperInfos=" + this.wallpaperInfos + ", packageName=" + this.packageName + "]";
    }
}
