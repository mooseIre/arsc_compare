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

    public String toString() {
        return "LockScreenMagazineWallpaperInfo [authority=" + this.authority + ", key=" + this.key + ", wallpaperUri=" + this.wallpaperUri + ", title=" + this.title + ", content=" + this.content + ", packageName=" + this.packageName + ", landingPageUrl=" + this.landingPageUrl + ", supportLike=" + this.supportLike + ", like=" + this.like + ", tag=" + this.tag + ", cp=" + this.cp + ", pos=" + this.pos + ", ex=" + this.ex + "]";
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0044 A[SYNTHETIC, Splitter:B:13:0x0044] */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00ac  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean opendAd(android.content.Context r7) {
        /*
            r6 = this;
            java.lang.String r0 = "StartActivityWhenLocked"
            java.lang.String r1 = "wallpaperinfo"
            r2 = 0
            r3 = 1
            java.lang.String r4 = r6.deeplinkUrl     // Catch:{ Exception -> 0x002b }
            boolean r4 = android.text.TextUtils.isEmpty(r4)     // Catch:{ Exception -> 0x002b }
            if (r4 != 0) goto L_0x0041
            java.lang.String r4 = r6.deeplinkUrl     // Catch:{ Exception -> 0x002b }
            android.content.Intent r4 = android.content.Intent.parseUri(r4, r2)     // Catch:{ Exception -> 0x002b }
            java.lang.String r5 = r6.packageName     // Catch:{ Exception -> 0x002b }
            boolean r5 = android.text.TextUtils.isEmpty(r5)     // Catch:{ Exception -> 0x002b }
            if (r5 != 0) goto L_0x0021
            java.lang.String r5 = r6.packageName     // Catch:{ Exception -> 0x002b }
            r4.setPackage(r5)     // Catch:{ Exception -> 0x002b }
        L_0x0021:
            r4.putExtra(r0, r3)     // Catch:{ Exception -> 0x002b }
            android.os.UserHandle r5 = android.os.UserHandle.CURRENT     // Catch:{ Exception -> 0x002b }
            r7.startActivityAsUser(r4, r5)     // Catch:{ Exception -> 0x002b }
            r4 = r3
            goto L_0x0042
        L_0x002b:
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "deeplinkUrl not found : "
            r4.append(r5)
            java.lang.String r5 = r6.deeplinkUrl
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            android.util.Log.e(r1, r4)
        L_0x0041:
            r4 = r2
        L_0x0042:
            if (r4 != 0) goto L_0x0081
            java.lang.String r5 = r6.landingPageUrl     // Catch:{ Exception -> 0x006b }
            boolean r5 = android.text.TextUtils.isEmpty(r5)     // Catch:{ Exception -> 0x006b }
            if (r5 != 0) goto L_0x0068
            java.lang.String r5 = r6.landingPageUrl     // Catch:{ Exception -> 0x006b }
            android.content.Intent r2 = android.content.Intent.parseUri(r5, r2)     // Catch:{ Exception -> 0x006b }
            java.lang.String r5 = r6.packageName     // Catch:{ Exception -> 0x006b }
            boolean r5 = android.text.TextUtils.isEmpty(r5)     // Catch:{ Exception -> 0x006b }
            if (r5 != 0) goto L_0x005f
            java.lang.String r5 = r6.packageName     // Catch:{ Exception -> 0x006b }
            r2.setPackage(r5)     // Catch:{ Exception -> 0x006b }
        L_0x005f:
            r2.putExtra(r0, r3)     // Catch:{ Exception -> 0x006b }
            android.os.UserHandle r0 = android.os.UserHandle.CURRENT     // Catch:{ Exception -> 0x006b }
            r7.startActivityAsUser(r2, r0)     // Catch:{ Exception -> 0x006b }
            goto L_0x0069
        L_0x0068:
            r3 = r4
        L_0x0069:
            r4 = r3
            goto L_0x0081
        L_0x006b:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r2 = "landingPageUrl not found : "
            r0.append(r2)
            java.lang.String r2 = r6.landingPageUrl
            r0.append(r2)
            java.lang.String r0 = r0.toString()
            android.util.Log.e(r1, r0)
        L_0x0081:
            if (r4 == 0) goto L_0x010e
            java.lang.String r0 = r6.authority
            boolean r0 = android.text.TextUtils.isEmpty(r0)
            if (r0 != 0) goto L_0x010e
            android.content.ContentResolver r0 = r7.getContentResolver()
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "content://"
            r2.append(r3)
            java.lang.String r3 = r6.authority
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            android.net.Uri r2 = android.net.Uri.parse(r2)
            android.content.IContentProvider r0 = r0.acquireProvider(r2)
            if (r0 == 0) goto L_0x010e
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "tarck ad key="
            r2.append(r3)
            java.lang.String r3 = r6.key
            r2.append(r3)
            java.lang.String r3 = ";authority="
            r2.append(r3)
            java.lang.String r3 = r6.authority
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            android.util.Log.d(r1, r2)
            org.json.JSONObject r1 = new org.json.JSONObject     // Catch:{ Exception -> 0x00fa }
            r1.<init>()     // Catch:{ Exception -> 0x00fa }
            java.lang.String r2 = "key"
            java.lang.String r3 = r6.key     // Catch:{ Exception -> 0x00fa }
            r1.put(r2, r3)     // Catch:{ Exception -> 0x00fa }
            java.lang.String r2 = "event"
            r3 = 2
            r1.put(r2, r3)     // Catch:{ Exception -> 0x00fa }
            android.os.Bundle r2 = new android.os.Bundle     // Catch:{ Exception -> 0x00fa }
            r2.<init>()     // Catch:{ Exception -> 0x00fa }
            java.lang.String r3 = "request_json"
            java.lang.String r1 = r1.toString()     // Catch:{ Exception -> 0x00fa }
            r2.putString(r3, r1)     // Catch:{ Exception -> 0x00fa }
            java.lang.String r1 = r7.getPackageName()     // Catch:{ Exception -> 0x00fa }
            java.lang.String r6 = r6.authority     // Catch:{ Exception -> 0x00fa }
            java.lang.String r3 = "recordEvent"
            r0.call(r1, r6, r3, r2)     // Catch:{ Exception -> 0x00fa }
            goto L_0x00fe
        L_0x00f8:
            r6 = move-exception
            goto L_0x0106
        L_0x00fa:
            r6 = move-exception
            r6.printStackTrace()     // Catch:{ all -> 0x00f8 }
        L_0x00fe:
            android.content.ContentResolver r6 = r7.getContentResolver()
            r6.releaseProvider(r0)
            goto L_0x010e
        L_0x0106:
            android.content.ContentResolver r7 = r7.getContentResolver()
            r7.releaseProvider(r0)
            throw r6
        L_0x010e:
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.magazine.entity.LockScreenMagazineWallpaperInfo.opendAd(android.content.Context):boolean");
    }
}
