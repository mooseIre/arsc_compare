package com.android.keyguard.wallpaper.entity;

public class MiuiWallpaperInfo {
    public String authority;
    public String content;
    public String cp;
    public String ex;
    public String key;
    public String landingPageUrl;
    public boolean like;
    public String packageName;
    public int pos;
    public boolean supportLike;
    public String tag;
    public String title;
    public String wallpaperUri;

    public String toString() {
        return "WallpaperInfo [authority=" + this.authority + ", key=" + this.key + ", wallpaperUri=" + this.wallpaperUri + ", title=" + this.title + ", content=" + this.content + ", packageName=" + this.packageName + ", landingPageUrl=" + this.landingPageUrl + ", supportLike=" + this.supportLike + ", like=" + this.like + ", tag=" + this.tag + ", cp=" + this.cp + ", pos=" + this.pos + ", ex=" + this.ex + "]";
    }
}
