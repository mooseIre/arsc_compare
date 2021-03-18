package com.android.keyguard.magazine.entity;

import android.text.TextUtils;
import android.util.Log;
import com.android.keyguard.wallpaper.entity.MiuiWallpaperInfo;
import org.json.JSONObject;

public class LockScreenMagazineWallpaperInfo extends MiuiWallpaperInfo {
    public String btnText;
    public String carouselDeeplink;
    public String deeplinkUrl;
    public String entryTitle;
    public String globalBtnText;
    public boolean isTitleCustomized;
    public int linkType = 0;
    public String provider;
    public String source;
    public String sourceColor;
    public String titleClickUri;

    public void initExtra() {
        if (!TextUtils.isEmpty(this.ex)) {
            try {
                JSONObject jSONObject = new JSONObject(this.ex);
                setLinkType(jSONObject);
                setEntryTitle(jSONObject);
                setTitleCustomized(jSONObject);
                setProvider(jSONObject);
                setSource(jSONObject);
                setSourceColor(jSONObject);
                setGlobalBtnText(jSONObject);
                setTitleClickUri(jSONObject);
                setImgLevel(jSONObject);
                setCarouselDeepLink(jSONObject);
            } catch (Exception e) {
                Log.e("LockScreenMagazineWallpaperInfo", "initExtra exception " + e.getMessage());
            }
        }
    }

    private void setLinkType(JSONObject jSONObject) {
        try {
            this.linkType = Integer.parseInt(jSONObject.optString("link_type"));
        } catch (Exception unused) {
            this.linkType = 0;
        }
    }

    private void setEntryTitle(JSONObject jSONObject) {
        try {
            this.entryTitle = jSONObject.optString("lks_entry_text");
        } catch (Exception unused) {
            this.entryTitle = null;
        }
    }

    private void setTitleCustomized(JSONObject jSONObject) {
        try {
            boolean z = true;
            if (jSONObject.optInt("title_customized") != 1) {
                z = false;
            }
            this.isTitleCustomized = z;
        } catch (Exception unused) {
            this.isTitleCustomized = false;
        }
    }

    private void setProvider(JSONObject jSONObject) {
        try {
            this.provider = jSONObject.optString("provider");
        } catch (Exception unused) {
            this.provider = null;
        }
    }

    private void setSource(JSONObject jSONObject) {
        try {
            this.source = jSONObject.optString("source");
        } catch (Exception unused) {
            this.source = null;
        }
    }

    private void setSourceColor(JSONObject jSONObject) {
        try {
            this.sourceColor = jSONObject.optString("source_color");
        } catch (Exception unused) {
        }
    }

    private void setGlobalBtnText(JSONObject jSONObject) {
        try {
            this.globalBtnText = jSONObject.optString("more_button_text");
        } catch (Exception unused) {
            this.globalBtnText = null;
        }
    }

    private void setTitleClickUri(JSONObject jSONObject) {
        try {
            this.titleClickUri = jSONObject.optString("title_click_uri");
        } catch (Exception unused) {
            this.titleClickUri = null;
        }
    }

    private void setImgLevel(JSONObject jSONObject) {
        try {
            jSONObject.optInt("img_level", 0);
        } catch (Exception unused) {
        }
    }

    private void setCarouselDeepLink(JSONObject jSONObject) {
        try {
            this.carouselDeeplink = jSONObject.optString("deeplink");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // com.android.keyguard.wallpaper.entity.MiuiWallpaperInfo
    public String toString() {
        return "LockScreenMagazineWallpaperInfo [authority=" + this.authority + ", key=" + this.key + ", wallpaperUri=" + this.wallpaperUri + ", title=" + this.title + ", content=" + this.content + ", packageName=" + this.packageName + ", landingPageUrl=" + this.landingPageUrl + ", supportLike=" + this.supportLike + ", like=" + this.like + ", tag=" + this.tag + ", cp=" + this.cp + ", pos=" + this.pos + ", ex=" + this.ex + "]";
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0044 A[SYNTHETIC, Splitter:B:13:0x0044] */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00ac  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean opendAd(android.content.Context r7) {
        /*
        // Method dump skipped, instructions count: 271
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.magazine.entity.LockScreenMagazineWallpaperInfo.opendAd(android.content.Context):boolean");
    }
}
