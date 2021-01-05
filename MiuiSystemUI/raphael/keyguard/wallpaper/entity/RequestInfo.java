package com.android.keyguard.wallpaper.entity;

import java.util.List;

public class RequestInfo {
    public MiuiWallpaperInfo currentWallpaperInfo;
    public int mode;
    public boolean needLast;
    public String packageName;
    public List<MiuiWallpaperInfo> wallpaperInfos;

    public RequestInfo(int i, MiuiWallpaperInfo miuiWallpaperInfo) {
        this.mode = i;
        this.currentWallpaperInfo = miuiWallpaperInfo;
    }

    public String toString() {
        return "RequestInfo [mode=" + this.mode + ", currentWallpaperInfo=" + this.currentWallpaperInfo + ", needLast=" + this.needLast + ", wallpaperInfos=" + this.wallpaperInfos + ", packageName=" + this.packageName + "]";
    }
}
