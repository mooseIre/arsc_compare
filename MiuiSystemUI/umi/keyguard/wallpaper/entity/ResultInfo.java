package com.android.keyguard.wallpaper.entity;

import java.util.List;

public class ResultInfo {
    public String dialogComponent;
    public String previewComponent;
    public List<MiuiWallpaperInfo> wallpaperInfos;

    public String toString() {
        return "ResultInfo [previewComponent=" + this.previewComponent + ", dialogComponent=" + this.dialogComponent + ", wallpaperInfos=" + this.wallpaperInfos + "]";
    }
}
