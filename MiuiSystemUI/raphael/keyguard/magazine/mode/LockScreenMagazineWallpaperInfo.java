package com.android.keyguard.magazine.mode;

import android.text.TextUtils;
import android.util.Log;
import org.json.JSONObject;

public class LockScreenMagazineWallpaperInfo {
    public String authority;
    public String btnText;
    public String carouselDeeplink;
    public String content;
    public String cp;
    public String deeplinkUrl;
    public String entryTitle;
    public String ex;
    public String globalBtnText;
    public int imgLevel = 0;
    public boolean isTitleCustomized;
    public String key;
    public String landingPageUrl;
    public boolean like;
    public int linkType = 0;
    public String packageName;
    public int pos;
    public String provider;
    public String source;
    public String sourceColor;
    public boolean supportLike;
    public String tag;
    public String title;
    public String titleClickUri;
    public String wallpaperUri;

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
            this.imgLevel = jSONObject.optInt("img_level", 0);
        } catch (Exception unused) {
            this.imgLevel = 0;
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

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0045 A[SYNTHETIC, Splitter:B:13:0x0045] */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x00ab  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean opendAd(android.content.Context r13) {
        /*
            r12 = this;
            java.lang.String r0 = "StartActivityWhenLocked"
            java.lang.String r1 = "wallpaperinfo"
            r2 = 0
            r3 = 1
            java.lang.String r4 = r12.deeplinkUrl     // Catch:{ Exception -> 0x002c }
            boolean r4 = android.text.TextUtils.isEmpty(r4)     // Catch:{ Exception -> 0x002c }
            if (r4 != 0) goto L_0x0042
            java.lang.String r4 = r12.deeplinkUrl     // Catch:{ Exception -> 0x002c }
            android.content.Intent r4 = android.content.Intent.parseUri(r4, r2)     // Catch:{ Exception -> 0x002c }
            java.lang.String r5 = r12.packageName     // Catch:{ Exception -> 0x002c }
            boolean r5 = android.text.TextUtils.isEmpty(r5)     // Catch:{ Exception -> 0x002c }
            if (r5 != 0) goto L_0x0022
            java.lang.String r5 = r12.packageName     // Catch:{ Exception -> 0x002c }
            r4.setPackage(r5)     // Catch:{ Exception -> 0x002c }
        L_0x0022:
            r4.putExtra(r0, r3)     // Catch:{ Exception -> 0x002c }
            android.os.UserHandle r5 = android.os.UserHandle.CURRENT     // Catch:{ Exception -> 0x002c }
            r13.startActivityAsUser(r4, r5)     // Catch:{ Exception -> 0x002c }
            r4 = r3
            goto L_0x0043
        L_0x002c:
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "deeplinkUrl not found : "
            r4.append(r5)
            java.lang.String r5 = r12.deeplinkUrl
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            android.util.Log.e(r1, r4)
        L_0x0042:
            r4 = r2
        L_0x0043:
            if (r4 != 0) goto L_0x0080
            java.lang.String r5 = r12.landingPageUrl     // Catch:{ Exception -> 0x006a }
            boolean r5 = android.text.TextUtils.isEmpty(r5)     // Catch:{ Exception -> 0x006a }
            if (r5 != 0) goto L_0x0080
            java.lang.String r5 = r12.landingPageUrl     // Catch:{ Exception -> 0x006a }
            android.content.Intent r2 = android.content.Intent.parseUri(r5, r2)     // Catch:{ Exception -> 0x006a }
            java.lang.String r5 = r12.packageName     // Catch:{ Exception -> 0x006a }
            boolean r5 = android.text.TextUtils.isEmpty(r5)     // Catch:{ Exception -> 0x006a }
            if (r5 != 0) goto L_0x0060
            java.lang.String r5 = r12.packageName     // Catch:{ Exception -> 0x006a }
            r2.setPackage(r5)     // Catch:{ Exception -> 0x006a }
        L_0x0060:
            r2.putExtra(r0, r3)     // Catch:{ Exception -> 0x006a }
            android.os.UserHandle r0 = android.os.UserHandle.CURRENT     // Catch:{ Exception -> 0x006a }
            r13.startActivityAsUser(r2, r0)     // Catch:{ Exception -> 0x006a }
            r4 = r3
            goto L_0x0080
        L_0x006a:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r2 = "landingPageUrl not found : "
            r0.append(r2)
            java.lang.String r2 = r12.landingPageUrl
            r0.append(r2)
            java.lang.String r0 = r0.toString()
            android.util.Log.e(r1, r0)
        L_0x0080:
            if (r4 == 0) goto L_0x0111
            java.lang.String r0 = r12.authority
            boolean r0 = android.text.TextUtils.isEmpty(r0)
            if (r0 != 0) goto L_0x0111
            android.content.ContentResolver r0 = r13.getContentResolver()
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "content://"
            r2.append(r3)
            java.lang.String r3 = r12.authority
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            android.net.Uri r2 = android.net.Uri.parse(r2)
            android.content.IContentProvider r0 = r0.acquireProvider(r2)
            if (r0 == 0) goto L_0x0111
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "tarck ad key="
            r2.append(r3)
            java.lang.String r3 = r12.key
            r2.append(r3)
            java.lang.String r3 = ";authority="
            r2.append(r3)
            java.lang.String r3 = r12.authority
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            android.util.Log.d(r1, r2)
            org.json.JSONObject r1 = new org.json.JSONObject     // Catch:{ Exception -> 0x00fd }
            r1.<init>()     // Catch:{ Exception -> 0x00fd }
            java.lang.String r2 = "key"
            java.lang.String r3 = r12.key     // Catch:{ Exception -> 0x00fd }
            r1.put(r2, r3)     // Catch:{ Exception -> 0x00fd }
            java.lang.String r2 = "event"
            r3 = 2
            r1.put(r2, r3)     // Catch:{ Exception -> 0x00fd }
            android.os.Bundle r11 = new android.os.Bundle     // Catch:{ Exception -> 0x00fd }
            r11.<init>()     // Catch:{ Exception -> 0x00fd }
            java.lang.String r2 = "request_json"
            java.lang.String r1 = r1.toString()     // Catch:{ Exception -> 0x00fd }
            r11.putString(r2, r1)     // Catch:{ Exception -> 0x00fd }
            java.lang.String r6 = r13.getPackageName()     // Catch:{ Exception -> 0x00fd }
            r7 = 0
            java.lang.String r8 = r12.authority     // Catch:{ Exception -> 0x00fd }
            java.lang.String r9 = "recordEvent"
            r10 = 0
            r5 = r0
            android.content.ContentProviderCompat.call(r5, r6, r7, r8, r9, r10, r11)     // Catch:{ Exception -> 0x00fd }
            goto L_0x0101
        L_0x00fb:
            r12 = move-exception
            goto L_0x0109
        L_0x00fd:
            r12 = move-exception
            r12.printStackTrace()     // Catch:{ all -> 0x00fb }
        L_0x0101:
            android.content.ContentResolver r12 = r13.getContentResolver()
            r12.releaseProvider(r0)
            goto L_0x0111
        L_0x0109:
            android.content.ContentResolver r13 = r13.getContentResolver()
            r13.releaseProvider(r0)
            throw r12
        L_0x0111:
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.magazine.mode.LockScreenMagazineWallpaperInfo.opendAd(android.content.Context):boolean");
    }
}
