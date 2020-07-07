package com.android.keyguard.magazine.mode;

import android.text.TextUtils;
import android.util.Log;
import org.json.JSONObject;

public class LockScreenMagazineWallpaperInfo {
    public String authority;
    public String btnText;
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

    public String toString() {
        return "LockScreenMagazineWallpaperInfo [authority=" + this.authority + ", key=" + this.key + ", wallpaperUri=" + this.wallpaperUri + ", title=" + this.title + ", content=" + this.content + ", packageName=" + this.packageName + ", landingPageUrl=" + this.landingPageUrl + ", supportLike=" + this.supportLike + ", like=" + this.like + ", tag=" + this.tag + ", cp=" + this.cp + ", pos=" + this.pos + ", ex=" + this.ex + "]";
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0045 A[SYNTHETIC, Splitter:B:13:0x0045] */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00ad  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean opendAd(android.content.Context r7) {
        /*
            r6 = this;
            java.lang.String r0 = "StartActivityWhenLocked"
            java.lang.String r1 = "wallpaperinfo"
            r2 = 0
            r3 = 1
            java.lang.String r4 = r6.deeplinkUrl     // Catch:{ Exception -> 0x002c }
            boolean r4 = android.text.TextUtils.isEmpty(r4)     // Catch:{ Exception -> 0x002c }
            if (r4 != 0) goto L_0x0042
            java.lang.String r4 = r6.deeplinkUrl     // Catch:{ Exception -> 0x002c }
            android.content.Intent r4 = android.content.Intent.parseUri(r4, r2)     // Catch:{ Exception -> 0x002c }
            java.lang.String r5 = r6.packageName     // Catch:{ Exception -> 0x002c }
            boolean r5 = android.text.TextUtils.isEmpty(r5)     // Catch:{ Exception -> 0x002c }
            if (r5 != 0) goto L_0x0022
            java.lang.String r5 = r6.packageName     // Catch:{ Exception -> 0x002c }
            r4.setPackage(r5)     // Catch:{ Exception -> 0x002c }
        L_0x0022:
            r4.putExtra(r0, r3)     // Catch:{ Exception -> 0x002c }
            android.os.UserHandle r5 = android.os.UserHandle.CURRENT     // Catch:{ Exception -> 0x002c }
            r7.startActivityAsUser(r4, r5)     // Catch:{ Exception -> 0x002c }
            r4 = r3
            goto L_0x0043
        L_0x002c:
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "deeplinkUrl not found : "
            r4.append(r5)
            java.lang.String r5 = r6.deeplinkUrl
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            android.util.Log.e(r1, r4)
        L_0x0042:
            r4 = r2
        L_0x0043:
            if (r4 != 0) goto L_0x0082
            java.lang.String r5 = r6.landingPageUrl     // Catch:{ Exception -> 0x006c }
            boolean r5 = android.text.TextUtils.isEmpty(r5)     // Catch:{ Exception -> 0x006c }
            if (r5 != 0) goto L_0x0069
            java.lang.String r5 = r6.landingPageUrl     // Catch:{ Exception -> 0x006c }
            android.content.Intent r2 = android.content.Intent.parseUri(r5, r2)     // Catch:{ Exception -> 0x006c }
            java.lang.String r5 = r6.packageName     // Catch:{ Exception -> 0x006c }
            boolean r5 = android.text.TextUtils.isEmpty(r5)     // Catch:{ Exception -> 0x006c }
            if (r5 != 0) goto L_0x0060
            java.lang.String r5 = r6.packageName     // Catch:{ Exception -> 0x006c }
            r2.setPackage(r5)     // Catch:{ Exception -> 0x006c }
        L_0x0060:
            r2.putExtra(r0, r3)     // Catch:{ Exception -> 0x006c }
            android.os.UserHandle r0 = android.os.UserHandle.CURRENT     // Catch:{ Exception -> 0x006c }
            r7.startActivityAsUser(r2, r0)     // Catch:{ Exception -> 0x006c }
            goto L_0x006a
        L_0x0069:
            r3 = r4
        L_0x006a:
            r4 = r3
            goto L_0x0082
        L_0x006c:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r2 = "landingPageUrl not found : "
            r0.append(r2)
            java.lang.String r2 = r6.landingPageUrl
            r0.append(r2)
            java.lang.String r0 = r0.toString()
            android.util.Log.e(r1, r0)
        L_0x0082:
            if (r4 == 0) goto L_0x010f
            java.lang.String r0 = r6.authority
            boolean r0 = android.text.TextUtils.isEmpty(r0)
            if (r0 != 0) goto L_0x010f
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
            if (r0 == 0) goto L_0x010f
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
            org.json.JSONObject r1 = new org.json.JSONObject     // Catch:{ Exception -> 0x00fb }
            r1.<init>()     // Catch:{ Exception -> 0x00fb }
            java.lang.String r2 = "key"
            java.lang.String r6 = r6.key     // Catch:{ Exception -> 0x00fb }
            r1.put(r2, r6)     // Catch:{ Exception -> 0x00fb }
            java.lang.String r6 = "event"
            r2 = 2
            r1.put(r6, r2)     // Catch:{ Exception -> 0x00fb }
            android.os.Bundle r6 = new android.os.Bundle     // Catch:{ Exception -> 0x00fb }
            r6.<init>()     // Catch:{ Exception -> 0x00fb }
            java.lang.String r2 = "request_json"
            java.lang.String r1 = r1.toString()     // Catch:{ Exception -> 0x00fb }
            r6.putString(r2, r1)     // Catch:{ Exception -> 0x00fb }
            java.lang.String r1 = r7.getPackageName()     // Catch:{ Exception -> 0x00fb }
            java.lang.String r2 = "recordEvent"
            r3 = 0
            r0.call(r1, r2, r3, r6)     // Catch:{ Exception -> 0x00fb }
            goto L_0x00ff
        L_0x00f9:
            r6 = move-exception
            goto L_0x0107
        L_0x00fb:
            r6 = move-exception
            r6.printStackTrace()     // Catch:{ all -> 0x00f9 }
        L_0x00ff:
            android.content.ContentResolver r6 = r7.getContentResolver()
            r6.releaseProvider(r0)
            goto L_0x010f
        L_0x0107:
            android.content.ContentResolver r7 = r7.getContentResolver()
            r7.releaseProvider(r0)
            throw r6
        L_0x010f:
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.magazine.mode.LockScreenMagazineWallpaperInfo.opendAd(android.content.Context):boolean");
    }
}
